# This is solution to codeeval.com Peak Traffic puzzle
# https://www.codeeval.com/open_challenges/49/

# ---------------------------- TEST CODE -------------------------

def read_lines(filename) 
	data = []
	File.open(filename, "r") do |infile|      
	  while (line = infile.gets)
		data << line.strip
	  end
	end
	data
end

def test_1
	pairs = [[1,2],[2,1],[1,3],[3,1],[2,3],[3,2],[4,5],[5,4],[4,6],[6,4],[5,6],[6,5]]
	build_clusters(pairs)
end

def test_2
	lines = read_lines("data.txt").map{|x| x.strip}
	clusters = solve(lines)
	clusters.each{|x| puts x}
end

# ---------------------------- MAIN CODE -------------------------
require 'set'

def unique_ids pairs
	s = Set.new
	pairs.each{|pair| x = pair.to_a; s << x[0]; s << x[1]}
	s
end

def proper_subset x, y
	x.subset?(y) and x != y
end

def dominating_sets sets
	np = Set.new
	sets.each{|s| np << s if !sets.any?{|x| proper_subset(s, x)}}
	np
end

def build_clusters pairs
	pairs = Set.new(pairs)
	clusters = Set.new
	pairs.each{|x| clusters << Set.new(x) if pairs.member?(x.reverse)}
	ids = unique_ids(clusters)
	ids.each do |id|
		newclusters = Set.new
		clusters.each do |clus|
			rel = true
			clus.each do |oid|
				rel = false if !pairs.member?([id, oid]) or !pairs.member?([oid, id])
			end
			rel ? newclusters << clus.union([id]) : newclusters << clus
		end
		clusters = newclusters
	end
	dominating_sets(clusters).to_a.map{|x| x.to_a.sort}.to_a
end

# Assume lines are single text lines which need to be parsed, but are already cleaned
def solve lines
	pairs = lines.map{|x| x.split(' ').reverse[0..1]}
	clus = build_clusters(pairs).select{|x| x.length > 2}.map{|x| x.join(', ')}.sort
end


begin
# Sample code to read in test cases:
	lines = []
	File.open(ARGV[0]).each_line do |line|
		lines << line.strip
	end
	clusters = solve(lines)
	clusters.each{|x| puts x}
end
