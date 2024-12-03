package processor.pipeline;

import generic.Instruction;
import generic.Operand;

public class EX_MA_LatchType {
	
	boolean MA_enable;
	int aluResult;
	Instruction instruction;
	Operand op2;
	
	
	public EX_MA_LatchType()
	{
		MA_enable = false;
	}

	public boolean isMA_enable() {
		return MA_enable;
	}

	public void setMA_enable(boolean mA_enable) {
		MA_enable = mA_enable;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setALUResult(int aluResult) {
		this.aluResult = aluResult;
	}

	public int getALUResult(){
		return aluResult;
	}

	public void setOp2(Operand op2) {
		this.op2 = op2;
	}

	public Operand getOp2(){
		return op2;
	}

}
