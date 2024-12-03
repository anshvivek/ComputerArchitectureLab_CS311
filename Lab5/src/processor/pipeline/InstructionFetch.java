package processor.pipeline;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.Simulator;
import generic.Statistics;
import processor.Clock;
import processor.Processor;

public class InstructionFetch implements Element{
	
	Processor containingProcessor;
	IF_EnableLatchType IF_EnableLatch;
	IF_OF_LatchType IF_OF_Latch;
	EX_IF_LatchType EX_IF_Latch;
	
	public InstructionFetch(Processor containingProcessor, IF_EnableLatchType iF_EnableLatch, IF_OF_LatchType iF_OF_Latch, EX_IF_LatchType eX_IF_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_EnableLatch = iF_EnableLatch;
		this.IF_OF_Latch = iF_OF_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}
	
	public void performIF()
	{
		if(true){
			if((EX_IF_Latch.isIF_enable() == true) && (IF_OF_Latch.isIF_branching_busy() == false) && (IF_OF_Latch.isIF_busy() == false)){
				Simulator.getEventQueue().addEvent(new MemoryReadEvent(Clock.getCurrentTime()+Configuration.mainMemoryLatency,this,containingProcessor.getMainMemory(),EX_IF_Latch.getPC()));
				containingProcessor.getRegisterFile().setProgramCounter(EX_IF_Latch.getPC());
				EX_IF_Latch.setIF_enable(false);
				IF_OF_Latch.setOF_enable(false);
				IF_OF_Latch.setIF_branching_busy(true);

			}
			else if(IF_EnableLatch.isIF_enable() == true)
			{
				if (IF_EnableLatch.isFreeze() == true){
					IF_EnableLatch.setFreeze(false);
				}
				else{
					if(IF_OF_Latch.isIF_busy() == true){
						return;
					}
					Simulator.getEventQueue().addEvent(new MemoryReadEvent(Clock.getCurrentTime()+ Configuration.mainMemoryLatency,this,containingProcessor.getMainMemory(),containingProcessor.getRegisterFile().getProgramCounter()));
					IF_OF_Latch.setOF_enable(false);
					IF_OF_Latch.setIF_busy(true);
				}
			}
		}
	}

	@Override
	public void handleEvent(Event e) {
		if(IF_OF_Latch.isOF_busy()|| IF_EnableLatch.isFreeze()){
			e.setEventTime(Clock.getCurrentTime()+1);
			Simulator.getEventQueue().addEvent(e);
			IF_EnableLatch.setFreeze(false);
			IF_OF_Latch.setOF_enable(true);
			return;
		}
		if(IF_OF_Latch.isIF_branching_busy()){
			IF_EnableLatch.setFreeze(false);
			IF_OF_Latch.setIF_branching_busy(false);
			containingProcessor.getOFUnit().Proceed = true;
			return;
		}
		MemoryResponseEvent event = (MemoryResponseEvent) e;
		Statistics.setNumberOfInstructions(Statistics.getNumberOfInstructions()+1);
		IF_OF_Latch.setInstruction(event.getValue());
		containingProcessor.getRegisterFile().setProgramCounter(containingProcessor.getRegisterFile().getProgramCounter() + 1);
		IF_OF_Latch.setOF_enable(true);
		IF_EnableLatch.setFreeze(false);
		IF_OF_Latch.setIF_busy(false);
	}

}