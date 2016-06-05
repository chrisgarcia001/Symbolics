#Sample code to read in test cases:
import sys

def all_perms(elems):
	if len(elems) == 0:
		return []
	if len(elems) == 1:
		return [elems]
	else:
		perms = []
		for i in range(len(elems)):
			prefix = elems[i]
			rest = elems[:i] + elems[i + 1:]
			suffixes = all_perms(rest)
			perms += map(lambda x: [prefix] + x, suffixes)
	return perms
						
with open(sys.argv[1], 'r') as test_cases:
	for test in test_cases:
		test = test.strip()
		if test != '':
			perm_string = ','.join(sorted(map(lambda x: ''.join(x), all_perms(list(test)))))
			print(perm_string)
