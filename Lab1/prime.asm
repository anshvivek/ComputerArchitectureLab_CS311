	.data
a:
	12
	.text
main:
	load %x0, $a, %x3
	subi %x3, 1, %x4
check:
	div %x3, %x4, %x5
	beq %x31, %x0, notprime
	subi %x4, 1, %x4
	addi %x6, 1, %x6
	bgt %x4, %x6, check
	addi %x10, 1, %x10
	end
notprime:
	subi %x10, 1, %10
	end
