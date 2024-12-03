	.data
a:
	12321
	.text
main:
	load %x0, $a, %x3
	load %x0, $a, %x6
flip:
	divi %x3, 10, %x3
	muli %x4, 10, %x4
	add %x4, %x31, %x4
	bgt %x3, %x0, flip
	beq %x4, %x6, palindrome
	subi %x10, 1, %x10
	end
palindrome:
	addi %x10, 1, %x10
	end