.data
a:
	70
	80
	20
	40
	10
	30
	90
	60
n:
	8
	.text
main:
	load %x0, $n, %x6
	subi %x6, 1, %x6
yew:
	add %x0, %x6, %x7
hola:
	load %x8, $a, %x3
	addi %x5, 1, %x5
	load %x5, $a, %x4
	subi %x7, 1, %x7
	bgt %x4, %x3, seq
	beq %x7, %x0, fus
	jmp hola
seq:
	store %x4, $a, %x8
	store %x3, $a, %x5
	beq %x7, %x0, fus
	jmp hola
fus:
	subi %x6, 1, %x6
	addi %x8, 1, %x8
	add %x8, %x0, %x5
	beq %x6, %x0, endl
	jmp yew
endl:
	end