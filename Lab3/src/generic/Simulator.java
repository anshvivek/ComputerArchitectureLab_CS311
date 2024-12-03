package generic;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.DataInputStream;

import processor.Clock;
import processor.Processor;

public class Simulator {
		
	static Processor processor;
	static boolean simulationComplete;
	
	public static void setupSimulation(String assemblyProgramFile, Processor p)
	{
		Simulator.processor = p;
		loadProgram(assemblyProgramFile);
		
		simulationComplete = false;
	}
	
	static void loadProgram(String assemblyProgramFile)
	{
		/*
		 * 1. load the program into memory according to the program layout described
		 *    in the ISA specification
		 * 2. set PC to the address of the first instruction in the main
		 * 3. set the following registers:
		 *     x0 = 0
		 *     x1 = 65535
		 *     x2 = 65535
		 */
		
		try (
			InputStream is = new FileInputStream(assemblyProgramFile);
		){
			DataInputStream d_is = new DataInputStream(is);
			int address = -1;
		while(d_is.available() > 0){
			int next = d_is.readInt();
			System.out.println(next);
			if(address != -1){
				processor.getMainMemory().setWord(address, next);
			}
			else{
				processor.getRegisterFile().setProgramCounter(next);
			}
			address += 1;
		}
			processor.getRegisterFile().setValue(0, 0);
			processor.getRegisterFile().setValue(1, 65535);
			processor.getRegisterFile().setValue(2, 65535);
			// Debug
			// System.out.println(processor.getRegisterFile().getProgramCounter());
			// System.out.println(processor.getMainMemory().getContentsAsString(0, 10));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void simulate()
	{
		while(simulationComplete == false)
		{
			processor.getIFUnit().performIF();
			Clock.incrementClock();
			processor.getOFUnit().performOF();
			Clock.incrementClock();
			processor.getEXUnit().performEX();
			Clock.incrementClock();
			processor.getMAUnit().performMA();
			Clock.incrementClock();
			processor.getRWUnit().performRW();
			Clock.incrementClock();
			
			Statistics.setNumberOfCycles(Statistics.getNumberOfCycles() + 5);
			Statistics.setNumberOfInstructions(Statistics.getNumberOfInstructions() + 1);
			
		}
	}
	
	public static void setSimulationComplete(boolean value)
	{
		simulationComplete = value;
	}
}
