# Solution for codeeval.com "Reverse Groups" problem

def rg input_str
	parts = input_str.split(";")
	k = parts[1].to_i
	nums = parts[0].split(",")
	i = 0; v = []
	while i < nums.length do
		nxt = nums[i..(i + k - 1)]
		nxt.reverse! if i + k <= nums.length
		nxt.each{|val| v << val}
		i += k
	end
	v.join(",")
end
