package processor.pipeline;

import generic.Simulator;
import generic.Instruction;
import generic.Instruction.OperationType;
import processor.Processor;

public class RegisterWrite {
	Processor containingProcessor;
	MA_RW_LatchType MA_RW_Latch;
	IF_EnableLatchType IF_EnableLatch;
	EX_MA_LatchType EX_MA_Latch;
	
	public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch, IF_EnableLatchType iF_EnableLatch , EX_MA_LatchType eX_MA_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
		this.EX_MA_Latch = eX_MA_Latch;
	}
	
	public void performRW()
	{
		if((MA_RW_Latch.isRW_enable()==true))
		{
			Instruction instruction = MA_RW_Latch.getInstruction();
			OperationType op_type = instruction.getOperationType();
			int alu_result = MA_RW_Latch.getALU_result();
			boolean proceed = true;

			if (op_type==OperationType.load)
			{	
				EX_MA_Latch.setMA_busy(false);
				int load_result = MA_RW_Latch.getLoad_result();
				int rd = instruction.getDestinationOperand().getValue();
				containingProcessor.getRegisterFile().setValue(rd, load_result);
			}
			else if (op_type==OperationType.end)
			{
				Simulator.setSimulationComplete(true);
				proceed = false;
			}
			else
			{
				if (op_type!=OperationType.store && op_type!= OperationType.jmp && op_type!= OperationType.beq && op_type!=OperationType.bne && op_type!=OperationType.blt && op_type!=OperationType.bgt)
				{
					containingProcessor.getRegisterFile().setValue(instruction.getDestinationOperand().getValue(), alu_result);
				}
			}

			if(proceed == true){
				IF_EnableLatch.setIF_enable(true);
			}
			else{
				IF_EnableLatch.setIF_enable(false);
			}
			
		}

	}

}
