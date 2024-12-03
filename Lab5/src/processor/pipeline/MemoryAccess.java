package processor.pipeline;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.ExecutionCompleteEvent;
import generic.Instruction;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;
import generic.Instruction.OperationType;

public class MemoryAccess implements Element{
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
		if((EX_MA_Latch.isMA_enable() == true) && (EX_MA_Latch.isMA_busy() == false))
		{	
			Instruction instruction = EX_MA_Latch.getInstruction();
			OperationType op_type = instruction.getOperationType();
			MA_RW_Latch.setALU_result(EX_MA_Latch.getALUResult());
			MA_RW_Latch.setInstruction(instruction);
			MA_RW_Latch.setRW_enable(true);

			switch(op_type){
				case load :{
					Simulator.getEventQueue().addEvent(new MemoryReadEvent(Clock.getCurrentTime() + Configuration.mainMemoryLatency,this,containingProcessor.getMainMemory() , EX_MA_Latch.getALUResult()));
					EX_MA_Latch.setMA_busy(true);
					MA_RW_Latch.setRW_enable(false);
					break;
				}
				case store :{
					Simulator.getEventQueue().addEvent(new MemoryWriteEvent(Clock.getCurrentTime()+Configuration.mainMemoryLatency,this,containingProcessor.getMainMemory(),EX_MA_Latch.getALUResult(),containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue())));
					EX_MA_Latch.setMA_busy(true);
					MA_RW_Latch.setRW_enable(false);
					break;
				}
				default :{
					break ;
				}
			}
		}
	}

	@Override
	public void handleEvent(Event e){
		if(e instanceof ExecutionCompleteEvent){
			MA_RW_Latch.setRW_enable(true);
			EX_MA_Latch.setMA_busy(false);
			return;
		}
		MemoryResponseEvent event = (MemoryResponseEvent) e;
		MA_RW_Latch.setRW_enable(true);
		MA_RW_Latch.setLoad_result(event.getValue());
	}

}
