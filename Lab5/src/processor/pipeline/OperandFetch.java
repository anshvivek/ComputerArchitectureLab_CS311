package processor.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import generic.Instruction;
import processor.Processor;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;
import generic.Operand;
import generic.Statistics;

public class OperandFetch {
	Processor containingProcessor;
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;
	IF_EnableLatchType IF_EnableLatch;
	static OperationType[] opTypes = OperationType.values();
	public boolean Proceed;
	Queue<Integer> queue;
	boolean isEnd;
	
	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch, IF_EnableLatchType iF_EnableLatch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
		isEnd = false;
		Proceed = true;
		queue = new LinkedList<>();
		int i =0;
		while(i < 2){
			queue.add(-1);
			queue.add(-1);
			i+=1;
		}
		queue.add(-1);
	}

	boolean checkdatahazard(int[] operands) {
		for(int i=0;i<operands.length;i++) {
			if(queue.contains(operands[i])) {
				return true;
			}
		}
		return false;
	}

	void updateQueue(int operand) {
		queue.poll();
		queue.add(operand);
	}

	public void setisEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public static int twoscompliment(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '1') {
				chars[i] = '0';
			} 
			else {
				chars[i] = '1';
			}
		}
		String s1 = new String(chars);
		int num = Integer.parseInt(s1, 2);
		num+=1;
		return num;
	}

	public void setProceed(boolean proceed) {
		Proceed = proceed;
		if (Proceed == false) {
			OF_EX_Latch.setEX_enable(false);
		}
	}

	public void performOF()
	{	
		int reg_to_add = -1;
		boolean noDataHazard = true;

		if(isEnd == true){
			IF_EnableLatch.setIF_enable(false);
			IF_OF_Latch.setOF_enable(false);
			OF_EX_Latch.setEX_enable(false);
			return;
		}
		if(OF_EX_Latch.isEX_busy()){
			IF_OF_Latch.setOF_busy(true);	
		}
		else{
			IF_OF_Latch.setOF_busy(false);
		}
		
		
		if (Proceed == false){
			OF_EX_Latch.setEX_enable(false);
		}
		else if(IF_OF_Latch.isOF_enable() && Proceed && !IF_OF_Latch.isOF_busy())
		{
			int R3_type_operators[] = {0,2,4,6,8,10,12,14,16,18,20};
			int R2I_type_operators[] = {1,3,5,7,9,11,13,15,17,19,21,22,23,25,26,27,28};
			int R1I_type_operators[] = {24,29};

			int instruction = IF_OF_Latch.getInstruction();
			Instruction instr = new Instruction();
			String bin_instr = Integer.toBinaryString(instruction);
			if (bin_instr.length() < 32) {
				int diff = 32 - bin_instr.length();
				for (int i = 0; i < diff; i++) {
					bin_instr =  "0" + bin_instr;
				}
			}

			int pp = containingProcessor.getRegisterFile().getProgramCounter();
			instr.setProgramCounter(pp);
			int opcode = Integer.parseInt(bin_instr.substring(0, 5), 2);
			instr.setOperationType(opTypes[opcode]);

			// check if the instruction is of type R3
			if (Arrays.stream(R3_type_operators).anyMatch(x -> x == opcode)) {
				Operand rs1 = new Operand();
				rs1.setOperandType(Operand.OperandType.Register);
				rs1.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));

				Operand rs2 = new Operand();
				rs2.setOperandType(Operand.OperandType.Register);
				rs2.setValue(Integer.parseInt(bin_instr.substring(10, 15), 2));

				Operand rd = new Operand();
				rd.setOperandType(Operand.OperandType.Register);
				rd.setValue(Integer.parseInt(bin_instr.substring(15, 20), 2));

				int op1 = containingProcessor.getRegisterFile().getValue(rs1.getValue());
				int op2 = containingProcessor.getRegisterFile().getValue(rs2.getValue());
				if (checkdatahazard(new int[] { rs1.getValue(), rs2.getValue() }) == false) {
					reg_to_add = rd.getValue();
					instr.setDestinationOperand(rd);
					instr.setSourceOperand1(rs1);
					instr.setSourceOperand2(rs2);
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setOp1(op1);
					OF_EX_Latch.setOp2(op2);
				}
				else{
					noDataHazard = false;
				}
			}
			else if (Arrays.stream(R2I_type_operators).anyMatch(x -> x == opcode)) {
				Operand rs1 = new Operand();
				rs1.setOperandType(Operand.OperandType.Register);
				rs1.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));

				Operand rd = new Operand();
				rd.setOperandType(Operand.OperandType.Register);				
				rd.setValue(Integer.parseInt(bin_instr.substring(10, 15), 2));
				
				int imm = Integer.parseInt(bin_instr.substring(15, 32), 2);
				if (bin_instr.charAt(15)=='1'){
					imm = -1*twoscompliment(bin_instr.substring(15, 32));
				}
				int op1 = containingProcessor.getRegisterFile().getValue(rs1.getValue());
				int op2 = containingProcessor.getRegisterFile().getValue(rd.getValue());
				
				if (checkdatahazard(new int[] { rs1.getValue(), rd.getValue()}) == false){
					if(opcode <= 22) { 
						reg_to_add = rd.getValue();
					}
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setImm(imm);
					OF_EX_Latch.setOp1(op1);
					OF_EX_Latch.setOp2(op2);
					instr.setDestinationOperand(rd);
					instr.setSourceOperand1(rs1);
				}
				else{
					noDataHazard = false;
				}
			}
			else if (Arrays.stream(R1I_type_operators).anyMatch(x -> x == opcode)) {
				if(opcode == 24){ // jmp
					Operand op = new Operand();
					String imm = bin_instr.substring(10, 32);
					int imm_val = Integer.parseInt(imm, 2);
					if (imm.charAt(0) == '1'){
						imm_val = -1*twoscompliment(imm);
					}
					if (imm_val == 0){
						op.setOperandType(OperandType.Register);
						op.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));
						instr.setSourceOperand1(op);
					}
					else{
						op.setOperandType(OperandType.Immediate);
						op.setValue(imm_val);
						instr.setSourceOperand1(op);
					}
					if (!checkdatahazard(new int[] { op.getValue() })) {
						OF_EX_Latch.setInstruction(instr);
						OF_EX_Latch.setImm(imm_val);
					}else{
						noDataHazard = false;
					}
				}
				else{ // opcode == 29 end
					Operand rd = new Operand();
					rd.setOperandType(Operand.OperandType.Register);
					rd.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));
	
					instr.setDestinationOperand(rd);
					OF_EX_Latch.setInstruction(instr);

				}
			}

			OF_EX_Latch.setEX_enable(noDataHazard);
			if(noDataHazard == false){
				Statistics.setDatahazards(Statistics.getDatahazards() + 1);
				IF_EnableLatch.setFreeze(true);
			}
			updateQueue(reg_to_add);
		}
		else{
			OF_EX_Latch.setEX_enable(false);
		}
		
	}

}
