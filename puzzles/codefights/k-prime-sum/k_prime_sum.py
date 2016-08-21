# Solution to k-prime-sum problem, described here: https://codefights.com/challenge/2QDGG86MYiobsbyPx/main

# Build a list of all primes from in the interval [1, n].
def primes(n):
	pms = set(range(2, n + 1))
	c = 2
	while c <= n / 2:
		mults = range(c, n + 1, c)
		pms = pms.difference(set(mults).difference([c]))
		c += 1
	return sorted(list(pms))

# Solve the puzzle - uses dynamic programming.
def primeSum(n, k):
	pms = primes(n)
	matrix = map(lambda x: map(lambda y: False, range(k + 1)), range(n + 1))
	matrix[0][0] = True
	for i in range(1, n + 1):
		for j in range(1, k + 1):
			for p in [x for x in pms if i - x >= 0]:
				if matrix[i - p][j - 1]:
					matrix[i][j] = True
	return matrix[n][k]
	