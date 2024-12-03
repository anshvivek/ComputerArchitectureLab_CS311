	.data
n:
	10
	.text
main:
	addi %x3, 65535, %x3
	load %x0, $n, %x7
	subi %x7, 2, %x7
	addi %x4, 0, %x4
	store %x4, 0, %x3
	addi %x5, 1, %x5
	subi %x3, 1, %x3
	store %x5, 0, %x3
fibonacci:
	add %x4, %x5, %x6
	subi %x3, 1, %x3
	store %x6, 0, %x3
	add %x5, %x0, %x4
	add %x6, %x0, %x5
	subi %x7, 1, %x7
	bgt %x7, %x0, fibonacci
	end
