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
		this.EX_MA_Latch = eX_MA_Latch;
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
	}
	
	public void performMA()
	{
		if(EX_MA_Latch.isMA_enable())
		{
			int alu_result = EX_MA_Latch.getALUResult();
			MA_RW_Latch.setALU_result(alu_result);
			Instruction instruction = EX_MA_Latch.getInstruction();
			
			OperationType op_type = instruction.getOperationType();
	
			if (op_type==OperationType.load)
			{
				int ld_res = containingProcessor.getMainMemory().getWord(alu_result);
				MA_RW_Latch.setLoad_result(ld_res);
			}
			else if (op_type==OperationType.store)
			{
				int st_val = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue());
				containingProcessor.getMainMemory().setWord(alu_result, st_val);
			}

			EX_MA_Latch.setMA_enable(false);
			MA_RW_Latch.setInstruction(instruction);
			MA_RW_Latch.setRW_enable(true);

		}
	}

}
