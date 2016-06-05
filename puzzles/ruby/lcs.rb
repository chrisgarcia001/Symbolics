def build_matrix rows, columns, initv= nil
  mat = []
  1.upto(rows) do
    row = []
    1.upto(columns) do
      row << initv
    end
    mat << row
  end
  mat
end

def fast_lcs a, b
  mat = build_matrix(a.length + 1, b.length + 1)
  (a.length).downto(0) do |i|
    (b.length).downto(0) do |j|
      if a[i] == nil or b[j] == nil
        mat[i][j] = 0
      elsif a[i] == b[j]
        mat[i][j] = 1 + mat[i + 1][j + 1]
      else
        p1 = mat[i + 1][j]
        p2 = mat[i][j + 1]
        mat[i][j] = [p1, p2].max
      end
    end
  end
  
  s = []
  i = 0; j = 0
  while i < a.length and j < b.length
    if a[i] == b[j]
      s << a[i]
      i += 1; j += 1
    elsif mat[i + 1][j] >= mat[i][j + 1]
      i += 1
    else
      j += 1
    end
  end
  s
end

def build_lcs string
  s1, s2 = string.split(";")
  fast_lcs(s1.split(''), s2.split('')).join('')
end

