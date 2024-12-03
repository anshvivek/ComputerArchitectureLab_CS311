package processor.pipeline;

import generic.Instruction;
import processor.Processor;
import generic.Instruction.OperationType;

public class MemoryAccess {
	Processor containingProcessor;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;
	
	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}
	
	public void performMA()
	{
		if(EX_MA_Latch.isMA_enable())
		{
			Instruction instruction = EX_MA_Latch.getInstruction();
			int alu_result = EX_MA_Latch.getALUResult();
			MA_RW_Latch.setALU_result(alu_result);
			OperationType op_type = instruction.getOperationType();
			if (op_type==OperationType.load)
			{
				int load_result = containingProcessor.getMainMemory().getWord(alu_result);
				MA_RW_Latch.setLoad_result(load_result);
			}
			else if (op_type==OperationType.store)
			{
				int val_store = containingProcessor.getRegisterFile().getValue(
				instruction.getSourceOperand1().getValue());
				containingProcessor.getMainMemory().setWord(alu_result, val_store);
			}
			
			MA_RW_Latch.setInstruction(instruction);
			MA_RW_Latch.setRW_enable(true);

		}
	}

}
