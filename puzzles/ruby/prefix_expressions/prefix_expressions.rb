
# An exp is a list of strings in prefix form. 
def prefix_reduce exp
	if exp.empty?
		return []
	elsif exp[0].to_i.to_s == exp[0]
		return exp
	else 
		op = exp[0]
		s1 = prefix_reduce(exp[1..exp.length])
		a = s1.first
		s2 = prefix_reduce(s1[1..s1.length])
		b = s2.first
		s3 = s2[1..s2.length]
		return [eval(a + op + b).to_s] + s3
	end
end

def prefix_eval text_exp
	res = prefix_reduce(text_exp.split(' '))
	res.empty? ? "0" : res[0]
end

begin
	 File.open(ARGV[0]).each_line do |line|
		 puts prefix_eval(line.strip)
	end
end