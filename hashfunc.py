def to_array(oi):
    res = []
    i=abs(oi)
    while i>0:
        i, mod = divmod(i,10)
        res.append(mod)
    if oi<0:
    	res.append(-1)
    elif oi==0:
    	res.append(0)
    res.reverse()
    return res

def hash(orig):
    num = to_array(orig)
    res = 13
    for i in range(len(num)):
        n = num[i]+1
        res = res + n**i * orig**((i)%2+1)
    return res*num[0]

results = set()

print(hash(-9999999))

perc = 0
for i in range(-25000000,25000000):

    if i%100000==0:
        perc+=0.002
        print(perc)
    h = hash(i)
    print(i, " ", h)
    if h in results:
        print("взломанно ", i, " ", h)
        #break
    else:
        results.add(h)