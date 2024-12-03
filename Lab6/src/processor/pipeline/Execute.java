package processor.pipeline;
import processor.Processor;
import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.ExecutionCompleteEvent;
import generic.Instruction;
import generic.Simulator;
import generic.Statistics;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;

public class Execute implements Element{
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
		if((OF_EX_Latch.isEX_enable() == true) && (OF_EX_Latch.isEX_busy() == false))
		{

			OperationType alu_op = OF_EX_Latch.getInstruction().getOperationType();
			long latency;
			if (alu_op == OperationType.mul ) {
				latency = Configuration.multiplier_latency;
			} 
			if ( alu_op == OperationType.muli) {
				latency = Configuration.multiplier_latency;
			} 
			else if (alu_op == OperationType.div) {
				latency = Configuration.divider_latency;
			}
			else if (alu_op == OperationType.divi) {
				latency = Configuration.divider_latency;
			} 
			else {
				latency = Configuration.ALU_latency;
			}

			Simulator.getEventQueue().addEvent(new ExecutionCompleteEvent(latency, this, this));
			EX_MA_Latch.setMA_enable(false);
			OF_EX_Latch.setEX_busy(true);
		}
	}

	@Override
	public void handleEvent(Event e) {
		if(EX_MA_Latch.isMA_busy()){
			e.setEventTime(e.getEventTime()+1);
			Simulator.getEventQueue().addEvent(e);
			return;
		}

		int imm = OF_EX_Latch.getImm();
		int op1 = OF_EX_Latch.getOp1();
		int op2 = OF_EX_Latch.getOp2();

		int p = 0;
		int alu_result = 0;
		int cur_pc = containingProcessor.getRegisterFile().getProgramCounter();
		Instruction instruction = OF_EX_Latch.getInstruction();
		OperationType alu_op = OF_EX_Latch.getInstruction().getOperationType();

		if(alu_op == OperationType.add){
			alu_result = op1 + op2;
		}
		if(alu_op == OperationType.addi){
			alu_result = op1 + imm;
		}
		if(alu_op == OperationType.sub){
			alu_result = op1 - op2;
		}
		if(alu_op == OperationType.subi){
			alu_result = op1 - imm;
		}
		if(alu_op == OperationType.mul){
			alu_result = op1 * op2;
		}
		if(alu_op == OperationType.muli){
			alu_result = op1 * imm;
		}
		if(alu_op == OperationType.div){
			alu_result = op1 / op2; 
			containingProcessor.getRegisterFile().setValue(31, op1 % op2);
		}
		if(alu_op == OperationType.divi){
			alu_result = op1 / imm; 
			containingProcessor.getRegisterFile().setValue(31, op1 % imm);
		}
		if(alu_op == OperationType.and){
			alu_result = op1 & op2;
		}
		if(alu_op == OperationType.andi){
			alu_result = op1 & imm;
		}
		if(alu_op == OperationType.or){
			alu_result = op1 | op2;
		}
		if(alu_op == OperationType.ori){
			alu_result = op1 | imm;
		}
		if(alu_op == OperationType.xor){
			alu_result = op1 ^ op2;
		}
		if(alu_op == OperationType.xori){
			alu_result = op1 ^ imm;
		}
		if(alu_op == OperationType.slt){
			if(op1 < op2){
				alu_result = 1;
			}
			else{
				alu_result = 0;
			}
		}
		if(alu_op == OperationType.slti){
			if(op1 < imm){
				alu_result = 1;
			}
			else{
				alu_result = 0;
			}
		}
		if(alu_op == OperationType.sll){
			containingProcessor.getRegisterFile().setValue(31, (int) Math.pow(2, op2));
			alu_result = op1 << op2;
		}
		if(alu_op == OperationType.slli){
			containingProcessor.getRegisterFile().setValue(31, (int) Math.pow(2, imm));
			alu_result = op1 << imm;
		}
		if(alu_op == OperationType.srl){
			containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (op2 - 1)));
			alu_result = op1 >>> op2;	
		}
		if(alu_op == OperationType.srli){
			containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (imm - 1)));
			alu_result = op1 >>> imm;	
		}
		if(alu_op == OperationType.sra){
			containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (op2 - 1)));
			alu_result = op1 >> op2;
		}
		if(alu_op == OperationType.srai){
			containingProcessor.getRegisterFile().setValue(31, op1 & (1 << (op2 - 1)));
			alu_result = op1 >> imm;
		}

		if(alu_op == OperationType.load){
			alu_result = op1 + imm;
		}
		if(alu_op == OperationType.store){
			alu_result = op2 + imm;
		}

		if(alu_op == OperationType.jmp){
			OperandType optype = instruction.getSourceOperand1().getOperandType();
			if (optype == OperandType.Register){
				imm = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue());
				}
			else{
				imm = OF_EX_Latch.getImm();
				}
			alu_result = cur_pc + imm ;
			EX_IF_Latch.setIF_enable(true);
			
			EX_IF_Latch.setPC(alu_result-1);
			p = 1;
			containingProcessor.getOFUnit().setProceed(false);
		}
		if(alu_op == OperationType.beq && op1==op2){
				p = 1;
				alu_result = cur_pc + imm;
				EX_IF_Latch.setPC(alu_result-1);
				EX_IF_Latch.setIF_enable(true);
				containingProcessor.getOFUnit().setProceed(false);
		}
		if(alu_op == OperationType.bne && op1 != op2){
				p = 1;
				alu_result = cur_pc + imm;
				EX_IF_Latch.setPC(alu_result-1);
				EX_IF_Latch.setIF_enable(true);
				containingProcessor.getOFUnit().setProceed(false);
		}
		if(alu_op == OperationType.blt && op1 < op2){
				p = 1;
				alu_result = cur_pc + imm;
				EX_IF_Latch.setPC(alu_result-1);
				EX_IF_Latch.setIF_enable(true);
				containingProcessor.getOFUnit().setProceed(false);
		}
		if(alu_op == OperationType.bgt && op1 > op2){
				p = 1;
				EX_IF_Latch.setIF_enable(true);
				alu_result = cur_pc + imm;
				EX_IF_Latch.setPC(alu_result-1);
				containingProcessor.getOFUnit().setProceed(false);
		}
		else if(alu_op == OperationType.end){
			containingProcessor.getOFUnit().setisEnd(true);
		}

		EX_MA_Latch.setInstruction(instruction);
		EX_MA_Latch.setALUResult(alu_result);

		if(p == 0){
			EX_MA_Latch.setMA_enable(true);
		}
		else {	
			Statistics.setControlhazards(Statistics.getControlhazards()+2);
		}
		OF_EX_Latch.setEX_busy(false);
	}
}
