# Play with DNA challenge from codeeval.com:
# https://www.codeeval.com/open_challenges/126/

require 'set'

def near_matches subseq, superseq, k
	subseq = subseq.split('')
	superseq = superseq.split('')
	chunks = []
	i = 0
	while i + subseq.length - 1 < superseq.length do
		chunks << superseq[i..(i + subseq.length - 1)]
		i += 1
	end
	mismatches = Proc.new do |x, y|
		mm = 0
		x.each_with_index{|v, i| mm += 1 if v != y[i]}
		mm
	end
	pairs = chunks.map{|x| [x, mismatches.call(x, subseq)]}
	h = {}
	pairs.each do |pair|
		str, mm = pair
		if mm <= k
			h[mm] = [] if !h[mm]
			h[mm] << str
		end
	end
	ordered = []
	h.keys.sort.each{|k| ordered += h[k].sort}
	ordered.map{|x| x.join('')}
end

def solve line
	subseq, k, superseq = line.split(' ')
	k = k.to_i
	near_matches(subseq, superseq, k)
end

begin
	File.open(ARGV[0]).each_line do |line|
		arr = solve(line.strip)
		if arr.empty?
			puts "No match"
		else
			puts arr.join(' ')
		end
	end
end
