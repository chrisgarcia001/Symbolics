# Solution to String Permutations on codeeval: 
# https://www.codeeval.com/open_challenges/14/

require 'set'

# Generate all permutations for a list.
def all_perms arr
	if arr.length == 1
		return [arr]
	else
		perms = []
		(0..(arr.length - 1)).each do |i|
			front = arr[i]
			rest = Array.new(arr)
			rest.delete_at i
			sub_perms = all_perms(rest)
			sub_perms.each{|sp| perms << [front] + sp}
		end
		return perms
	end
end

# Read in string and produce output string solution.
def string_perms str
	arr = str.split('')
	perms = Set.new(all_perms(arr).map{|x| x.join('')}).to_a.sort
	perms.join(',')
end