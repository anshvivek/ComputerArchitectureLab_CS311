package processor.pipeline;

import java.util.Arrays;

import generic.Instruction;
import processor.Processor;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;
import generic.Operand;

public class OperandFetch {
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;
	Processor containingProcessor;
	static OperationType[] opTypes = OperationType.values();
	
	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch)
	{
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;
		this.containingProcessor = containingProcessor;
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
		if(IF_OF_Latch.isOF_enable())
		{	
			int R3[] = {0,2,4,6,8,10,12,14,16,18,20};
			int R2I[] = {1,3,5,7,9,11,13,15,17,19,21,22,23,25,26,27,28};
			int R1I[] = {24,29};

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

			if (Arrays.stream(R3).anyMatch(x -> x == opcode)) {
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

				instr.setDestinationOperand(rd);
				instr.setSourceOperand1(rs1);
				instr.setSourceOperand2(rs2);
				OF_EX_Latch.setInstruction(instr);
				OF_EX_Latch.setOp1(op1);
				OF_EX_Latch.setOp2(op2);
				
			}
			else if (Arrays.stream(R2I).anyMatch(x -> x == opcode)) {
				Operand rs1 = new Operand();
				Operand rd = new Operand();

				rs1.setOperandType(Operand.OperandType.Register);
				rs1.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));

				rd.setOperandType(Operand.OperandType.Register);				
				rd.setValue(Integer.parseInt(bin_instr.substring(10, 15), 2));
				
				int imm = Integer.parseInt(bin_instr.substring(15, 32), 2); // TODO: 2's complement
				if (bin_instr.charAt(15)=='1'){
					imm = -1*twoscompliment(bin_instr.substring(15, 32));
				}
				
				int op2 = containingProcessor.getRegisterFile().getValue(rd.getValue());
				int op1 = containingProcessor.getRegisterFile().getValue(rs1.getValue());

				OF_EX_Latch.setImm(imm);
				OF_EX_Latch.setOp1(op1);
				OF_EX_Latch.setOp2(op2);
				OF_EX_Latch.setInstruction(instr);

				instr.setSourceOperand1(rs1);
				instr.setDestinationOperand(rd);
			}
			else if (Arrays.stream(R1I).anyMatch(x -> x == opcode)) {
				if(opcode == 24){
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
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setImm(imm_val);
				}
				else{
					Operand rd = new Operand();
					rd.setOperandType(Operand.OperandType.Register);
					rd.setValue(Integer.parseInt(bin_instr.substring(5, 10), 2));
	
					instr.setDestinationOperand(rd);
					
					int imm = Integer.parseInt(bin_instr.substring(10, 32), 2); // TODO: 2's complement
					if (bin_instr.charAt(10)=='1'){
						imm = -1*twoscompliment(bin_instr.substring(10, 32));
						System.out.println(bin_instr);
					}
					OF_EX_Latch.setInstruction(instr);
					OF_EX_Latch.setImm(imm);

				}
			}

			IF_OF_Latch.setOF_enable(false);
			OF_EX_Latch.setEX_enable(true);
		}
	}

}