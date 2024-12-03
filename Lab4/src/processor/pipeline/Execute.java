package processor.pipeline;
import processor.Processor;

import generic.Instruction;
import generic.Statistics;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;

public class Execute {
	Processor containingProcessor;
	OF_EX_LatchType OF_EX_Latch;
	EX_MA_LatchType EX_MA_Latch;
	EX_IF_LatchType EX_IF_Latch;
	
	public Execute(Processor containingProcessor, OF_EX_LatchType oF_EX_Latch, EX_MA_LatchType eX_MA_Latch, EX_IF_LatchType eX_IF_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.OF_EX_Latch = oF_EX_Latch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}
	
	public void performEX()
	{
		// storing x31 here itself to not to complicate.
		// TODO:remove this later in pipeline
		if(OF_EX_Latch.isEX_enable())
		{
			int imm = OF_EX_Latch.getImm();
			int op1 = OF_EX_Latch.getOp1();
			int op2 = OF_EX_Latch.getOp2();
			
			int alu_result = 0 ;
			Instruction instruction = OF_EX_Latch.getInstruction();
			int cur_pc = containingProcessor.getRegisterFile().getProgramCounter();

			// System.out.println("EX: " + instruction);
			OperationType alu_op = OF_EX_Latch.getInstruction().getOperationType();
			// System.out.println("ALU OP: " + alu_op);
			boolean p = false;
			Statistics.setNumberOfInstructions(Statistics.getNumberOfInstructions() + 1);
			switch(alu_op)
			{
				case add: alu_result = op1 + op2; break;
				case sub: alu_result = op1 - op2; break;
				case mul: alu_result = op1 * op2; break;
				case div: alu_result = op1 / op2; containingProcessor.getRegisterFile().setValue(31, op1 % op2);break;
				case addi: alu_result = op1 + imm; break;
				case subi: alu_result = op1 - imm; break;
				case muli: alu_result = op1 * imm; break;
				case divi: alu_result = op1 / imm; containingProcessor.getRegisterFile().setValue(31, op1 % imm);break;
				case and: alu_result = op1 & op2; break;
				case or: alu_result = op1 | op2; break;
				case xor: alu_result = op1 ^ op2; break;
				case andi: alu_result = op1 & imm; break;
				case ori: alu_result = op1 | imm; break;
				case xori: alu_result = op1 ^ imm; break;
				case slt: alu_result= (op1 < op2) ? 1 : 0; break;
				case sll:
				containingProcessor.getRegisterFile().setValue(31, (int) Math.pow(2, op2));
				alu_result = op1 << op2;
				break;
				case srl:
				containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (op2 - 1)));
				alu_result = op1 >>> op2;
				break;
				case sra:
				containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (op2 - 1)));
				alu_result = op1 >> op2;
				break;
				case slti: alu_result = (op1 < imm) ? 1 : 0; break;
				case slli: 
				containingProcessor.getRegisterFile().setValue(31, (int) Math.pow(2, imm));
				alu_result = op1 << imm;
				break;
				case srli:
				containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (imm - 1)));
				alu_result = op1 >>> imm;
				break;
				case srai:
				containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (imm - 1)));
				alu_result = op1 >> imm;
				break;

				case load: alu_result = op1 + imm; break;
				case store: alu_result = op2 + imm; break;
				case jmp:
				{
					OperandType optype = instruction.getSourceOperand1().getOperandType();
					if (optype == OperandType.Register){
						imm = containingProcessor.getRegisterFile().getValue(
							instruction.getSourceOperand1().getValue());
						}
					else{
						imm = OF_EX_Latch.getImm();
						}
					alu_result = cur_pc + imm ;
					EX_IF_Latch.setIF_enable(true);
					
					EX_IF_Latch.setPC(alu_result-1);
					p = true;
					containingProcessor.getOFUnit().setProceed(false);
				}
				break;
				case end:
				{
					containingProcessor.getRegisterFile().setProgramCounter(containingProcessor.getRegisterFile().getProgramCounter()-1);
					containingProcessor.getOFUnit().setisEnd(true);
					break;
				}
				case bne:
				{
					if(op1 != op2)
					{
						alu_result = cur_pc + imm;
						EX_IF_Latch.setIF_enable(true);
						EX_IF_Latch.setPC(alu_result-1);
						p = true;
						containingProcessor.getOFUnit().setProceed(false);
					}
				}
				break;

				case beq:
				{
					if(op1 == op2)
					{
						EX_IF_Latch.setIF_enable(true);
						alu_result = cur_pc + imm;
						EX_IF_Latch.setPC(alu_result-1);
						p = true ;
						containingProcessor.getOFUnit().setProceed(false);
					}
				}
				break;
				
				case blt:
				{

					if(op1 < op2)
					{
						alu_result = cur_pc + imm;
						EX_IF_Latch.setIF_enable(true);
						EX_IF_Latch.setPC(alu_result-1);
						p = true;
						containingProcessor.getOFUnit().setProceed(false);
						// System.out.println("hello world");
					}
					// System.out.println("hello world2");
				}
				break;
				case bgt:
				{
					if(op1 > op2)
					{
						alu_result = cur_pc + imm;
						EX_IF_Latch.setIF_enable(true);
						EX_IF_Latch.setPC(alu_result-1);
						p = true;
						containingProcessor.getOFUnit().setProceed(false);
					}
				}
				break;
				
				default:
					break;
				
			}
			EX_MA_Latch.setInstruction(OF_EX_Latch.getInstruction());
			EX_MA_Latch.setALUResult(alu_result);
			
			if(p){
				Statistics.setControlhazards(Statistics.getControlhazards()+2); // stall 2 cycles
				containingProcessor.getOFUnit().setProceed(false);
			}
			EX_MA_Latch.setMA_enable(true);
		}
	}
}
