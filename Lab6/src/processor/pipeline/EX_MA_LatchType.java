package processor.pipeline;

import generic.Instruction;
import generic.Operand;

public class EX_MA_LatchType {
	boolean MA_busy;
	boolean MA_enable;
	Operand op2;
	Instruction instruction;
	int aluResult;
	
	public EX_MA_LatchType()
	{
		MA_enable = false;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setOp2(Operand op2) {
		this.op2 = op2;
	}

	public Operand getOp2(){
		return op2;
	}

	public boolean isMA_busy() {
		return MA_busy;
	}

	public void setMA_busy(boolean mA_busy) {
		MA_busy = mA_busy;
	}

	public void setALUResult(int aluResult) {
		this.aluResult = aluResult;
	}

	public int getALUResult(){
		return aluResult;
	}

	public boolean isMA_enable() {
		return MA_enable;
	}

	public void setMA_enable(boolean mA_enable) {
		MA_enable = mA_enable;
	}

}
