package generic;

import java.io.*;
import java.util.*;
import java.nio.ByteBuffer;
import generic.Instruction.OperationType;

public class Simulator {

	static FileInputStream inputcodeStream = null;
	public static HashMap<OperationType, String> opHashMap = new HashMap<OperationType, String>() {
		{
			put(OperationType.add, "00000");
			put(OperationType.addi, "00001");
			put(OperationType.sub, "00010");
			put(OperationType.subi, "00011");
			put(OperationType.mul, "00100");
			put(OperationType.muli, "00101");
			put(OperationType.div, "00110");
			put(OperationType.divi, "00111");
			put(OperationType.and, "01000");
			put(OperationType.andi, "01001");
			put(OperationType.or, "01010");
			put(OperationType.ori, "01011");
			put(OperationType.xor, "01100");
			put(OperationType.xori, "01101");
			put(OperationType.slt, "01110");
			put(OperationType.slti, "01111");
			put(OperationType.sll, "10000");
			put(OperationType.slli, "10001");
			put(OperationType.srl, "10010");
			put(OperationType.srli, "10011");
			put(OperationType.sra, "10100");
			put(OperationType.srai, "10101");
			put(OperationType.load, "10110");
			put(OperationType.store, "10111");
			put(OperationType.jmp, "11000");
			put(OperationType.beq, "11001");
			put(OperationType.bne, "11010");
			put(OperationType.blt, "11011");
			put(OperationType.bgt, "11100");
			put(OperationType.end, "11101");
		}
	};

	public static void setupSimulation(String assemblyProgramFile) {
		int firstCodeAddress = ParsedProgram.parseDataSection(assemblyProgramFile);
		ParsedProgram.parseCodeSection(assemblyProgramFile, firstCodeAddress);
		ParsedProgram.printState();
	}

	public static String InttoBinaryString(int num, int numbits) {
		String binary = Integer.toBinaryString(num);

		if (binary.length() < numbits) {
			int numLeadingzeros = numbits - binary.length();
			String numberofzeroes = "0".repeat(numLeadingzeros);
			binary = numberofzeroes + binary;
		}
		return binary;
	}

	public static void assemble(String objectProgramFile) {
		// TODO your assembler code
		// 1. open the objectProgramFile in binary mode
		// 2. write the firstCodeAddress to the file
		// 3. write the data to the file
		// 4. assemble one instruction at a time, and write to the file
		// 5. close the file
		// string to binary
		// program counter

		FileOutputStream file;
		try {
			file = new FileOutputStream(objectProgramFile);
			file.write(ByteBuffer.allocate(4).putInt(ParsedProgram.firstCodeAddress).array());

			for (int i = 0; i < ParsedProgram.data.size(); i++)
				file.write(ByteBuffer.allocate(4).putInt(ParsedProgram.data.get(i)).array());

			for (generic.Instruction i : ParsedProgram.code) {
				String binary_str_inst = "";
				binary_str_inst += opHashMap.get(i.getOperationType());

				int pc = i.getProgramCounter();

				switch (i.getOperationType()) {
					// R3 type
					case add:
					case sub:
					case mul:
					case div:
					case and:
					case or:
					case xor:
					case slt:
					case sll:
					case srl:
					case sra: {
						binary_str_inst += InttoBinaryString(i.getSourceOperand1().getValue(), 5);
						binary_str_inst += InttoBinaryString(i.getSourceOperand2().getValue(), 5);
						binary_str_inst += InttoBinaryString(i.getDestinationOperand().getValue(), 5);
						binary_str_inst += InttoBinaryString(0, 12);
						break;
					}
					// r2 type
					case addi:
					case subi:
					case muli:
					case divi:
					case andi:
					case ori:
					case xori:
					case slti:
					case slli:
					case srli:
					case srai:
					case load:
					case store: {
						binary_str_inst += InttoBinaryString(i.getSourceOperand1().getValue(), 5);
						binary_str_inst += InttoBinaryString(i.getDestinationOperand().getValue(), 5);
						binary_str_inst += InttoBinaryString(i.getSourceOperand2().getValue(), 17);
						break;
					}

					case beq:
					case bne:
					case blt:
					case bgt: {
						binary_str_inst += InttoBinaryString(i.getSourceOperand1().getValue(), 5);
						binary_str_inst += InttoBinaryString(i.getSourceOperand2().getValue(), 5);
						int offset = ParsedProgram.symtab.get(i.getDestinationOperand().getLabelValue()) - pc;

						String Stringoffset = InttoBinaryString(offset, 17);

						if (Stringoffset.length() > 17) {
							binary_str_inst += Stringoffset.substring(Stringoffset.length() - 17);
						} else {
							binary_str_inst += Stringoffset;
						}

						break;
					}

					// RI type :
					case jmp: {
						if (i.destinationOperand.getOperandType() == Operand.OperandType.Register) {
							binary_str_inst += InttoBinaryString(i.getDestinationOperand().getValue(), 5);
							binary_str_inst += InttoBinaryString(0, 22);
						}

						else {
							binary_str_inst += InttoBinaryString(0, 5);
							int offset = ParsedProgram.symtab.get(i.getDestinationOperand().getLabelValue()) - pc;
							String Stringoffset = InttoBinaryString(offset, 22);
							if (Stringoffset.length() > 22) {
								binary_str_inst += Stringoffset.substring(Stringoffset.length() - 22);
							} else {
								binary_str_inst += Stringoffset;
							}
						}
						break;
					}

					case end: {
						binary_str_inst += InttoBinaryString(0, 27);
						break;
					}

				}
				int InstInteger = (int) Long.parseLong(binary_str_inst, 2);
				byte[] InstBinary = ByteBuffer.allocate(4).putInt(InstInteger).array();
				file.write(InstBinary);
			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error writing to the file: " + e.getMessage());
			return;
		}
	}
}
