package processor.pipeline;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

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
		for(int i=0; i<3; i++){
			queue.add(-1);
		}
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

	public static int twoscompliment(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '1') {
				chars[i] = '0';
			} else {
				chars[i] = '1';
			}
		}
		String one_c = new String(chars);
		int num = Integer.parseInt(one_c, 2);
		num += 1 ;
		return num;
	}
	
	public void performOF()
	{
		if(isEnd){
			IF_EnableLatch.setIF_enable(false);
			IF_OF_Latch.setOF_enable(false);
			OF_EX_Latch.setEX_enable(false);
			return;
		}
		int addtoqueue = -1;
		boolean noDataHazard = true;
		if(IF_OF_Latch.isOF_enable() && Proceed)
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

			instr.setProgramCounter(containingProcessor.getRegisterFile().getProgramCounter());
			int opcode = Integer.parseInt(bin_instr.substring(0, 5), 2);
			instr.setOperationType(opTypes[opcode]);

			// check if the instruction is of type R3
			if (Arrays.stream(R3_type_operators).anyMatch(x -> x == opcode)) {
				Operand rs1 = new Operand();
				Operand rs2 = new Operand();
				Operand rd = new Operand();
				rs1.setOperandType(Operand.OperandType.Register);
				rs1.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));

				rs2.setOperandType(Operand.OperandType.Register);
				rs2.setValue(Integer.parseInt(bin_instr.substring(10, 15), 2));

				rd.setOperandType(Operand.OperandType.Register);
				rd.setValue(Integer.parseInt(bin_instr.substring(15, 20), 2));

				int op1 = containingProcessor.getRegisterFile().getValue(rs1.getValue());
				int op2 = containingProcessor.getRegisterFile().getValue(rs2.getValue());
				if (checkdatahazard(new int[] { rs1.getValue(), rs2.getValue() })) {
					noDataHazard = false;
				}else{
					addtoqueue = rd.getValue();
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setOp1(op1);
					OF_EX_Latch.setOp2(op2);
					instr.setDestinationOperand(rd);
					instr.setSourceOperand1(rs1);
					instr.setSourceOperand2(rs2);
				}
			}
			else if (Arrays.stream(R2I_type_operators).anyMatch(x -> x == opcode)) {
				Operand rs1 = new Operand();
				Operand rd = new Operand();
				rs1.setOperandType(Operand.OperandType.Register);
				rs1.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));

				rd.setOperandType(Operand.OperandType.Register);				
				rd.setValue(Integer.parseInt(bin_instr.substring(10, 15), 2));
				
				// check 15th bit to see if it is negative
				int imm = Integer.parseInt(bin_instr.substring(15, 32), 2);
				if (bin_instr.charAt(15)=='1'){
					imm = -1*twoscompliment(bin_instr.substring(15, 32));
					// System.out.println(bin_instr);
				}
				int op1 = containingProcessor.getRegisterFile().getValue(rs1.getValue());
				int op2 = containingProcessor.getRegisterFile().getValue(rd.getValue());
				// System.out.println("imm: " + imm);
				
				if (checkdatahazard(new int[] { rs1.getValue(), rd.getValue()})){
					noDataHazard = false;
				}else{
					if(opcode <= 22) { // > 21 means it is a branch instruction so no need to update queue
						addtoqueue = rd.getValue();
					}
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setImm(imm);
					OF_EX_Latch.setOp1(op1);
					OF_EX_Latch.setOp2(op2);
					instr.setDestinationOperand(rd);
					instr.setSourceOperand1(rs1);
				}
			}
			else if (Arrays.stream(R1I_type_operators).anyMatch(x -> x == opcode)) {
				if(opcode != 24){ // end
					Operand rd = new Operand();
					rd.setOperandType(Operand.OperandType.Register);
					rd.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));
	
					instr.setDestinationOperand(rd);
					OF_EX_Latch.setInstruction(instr);
				}
				else{ // opcode == 24 jmp
					Operand op = new Operand();
					String imm = bin_instr.substring(10, 32);
					int imm_val = Integer.parseInt(imm, 2);
					if (imm.charAt(0) == '1'){
						imm_val = -1*twoscompliment(imm);
					}
					if (imm_val != 0){
						op.setOperandType(OperandType.Immediate);
						op.setValue(imm_val);
						instr.setSourceOperand1(op);
					}
					else{
						op.setOperandType(OperandType.Register);
						op.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));
						instr.setSourceOperand1(op);
					}
					if (checkdatahazard(new int[] { op.getValue() })) {
						noDataHazard = false;
					}else{
						OF_EX_Latch.setInstruction(instr);
						OF_EX_Latch.setImm(imm_val);
					}
				}
			}

			OF_EX_Latch.setEX_enable(noDataHazard);
			if(!noDataHazard){
				IF_EnableLatch.setFreeze(true);
				System.out.println("\n\nData Hazard - Interlock\n\n");
				Statistics.setDatahazards(Statistics.getDatahazards() + 1);
			}
		}
		else if (!Proceed) {
			// Proceed = true;
			System.out.println("\n\nControl Hazard - Interlock\n\n");
			Proceed = true ;
		}
		updateQueue(addtoqueue);
	}

	public void setisEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}

	public void setProceed(boolean proceed) {
		Proceed = proceed;
		if (!Proceed) {
			OF_EX_Latch.setEX_enable(false);
		}
	}

}
