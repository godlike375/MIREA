from math import gcd
import random

#быстрое возведение в степень по модулю
def mod_pow(base, exp, mod):
    if exp == 0: return 1
    if exp & 1 == 0:
        r = mod_pow(base, exp // 2, mod)
        return (r * r) % mod
    else: return (base % mod * mod_pow(base, exp - 1, mod)) % mod
#тест рабина миллера
def miller(n, k):
    if (n <= 1 or n == 4):
        return False
    if (n <= 3):
        return True
    d = n - 1
    s = 0
    while (d % 2 == 0):
        d //= 2
        s = s+1
    for i in range(k):
        a = random.randint(2, n - 2)
        x = mod_pow(a,d,n)
        fc = False
        if (x == 1 or x == n - 1):
            continue
        for j in range(s-1):
            x = (x*x) % n
            if x==1:
                return False
            elif x==n-1:
                fc = True
                break
        if fc == True:
            continue
        return False
    return True
#быстрая проверка для маленьких простых чисел
def isPrime(n):
    for i in [i*i for i in range(2, int(n**0.5))]:
        if n % i == 0:
            return False
    return True
#генерация маленьких простых чисел
def littlePrimes(n):
    primes = []
    if n >= 2:
        primes.append(2)
    for i in range(2, n, 2):
        if isPrime(i):
            primes.append(i)
    return primes


#генерация больших простых чисел
def genPrime(fr, to, k=5):
    lowPrimeNum = littlePrimes(int(to**0.5))
    prime = False
    randNum = 0
    while not prime:
        prime = True
        randNum = random.randint(fr,to)
        #generate 1[0...0]1 number to set the first and last bits in randNum
        temp = int('1'+''.join(['0' for i in range(randNum.bit_length()-2 )]) +'1', 2)
        randNum = randNum | temp


        for i in range(len(lowPrimeNum)):
            if randNum % lowPrimeNum[i] == 0:
                prime = False
                break
        if not prime:
            continue
        if miller(randNum, k) == True:
            return randNum
        prime = False
        continue

#Диффи Хелман
def DiffHell(pubKeyA, pubKeyB):
    privKeyA = genPrime(1000,10000,5)
    privKeyB = genPrime(1000,10000,5)

    openKeyA = mod_pow(pubKeyA, privKeyA, pubKeyB)
    openKeyB = mod_pow(pubKeyA, privKeyB, pubKeyB)

    endKeyA = mod_pow(openKeyB, privKeyA, pubKeyB)
    endKeyB = mod_pow(openKeyA, privKeyB, pubKeyB)

    if endKeyA == endKeyB:
        return endKeyA, endKeyB
    else:
        raise Exception("failed")

print("diffie hellman", DiffHell(87, 121))

def xgcd(a, b):
#(g, x, y) -> a*x + b*y = g
    x0, x1, y0, y1 = 0, 1, 1, 0
    while a != 0:
        (q, a), b = divmod(b, a), a
        y0, y1 = y1, y0 - q * y1
        x0, x1 = x1, x0 - q * x1
    return b, x0, y0
#обратный модуль
def modinv(a, m):
    g, x, y = xgcd(a, m)
    if g != 1:
        raise Exception('modular inverse does not exist')
    else:
        return x % m


#алгоритм RSA
def RSA(msg):
    p, q = genPrime(2000,10000,5), genPrime(2000,10000,5)

    n = p*q

    phi = (p-1)*(q-1)
    e = 2
    while gcd(e, phi)>1 and isPrime(e):
        e = e+1

    d = modinv(e, phi)

    x = mod_pow(msg, e, n)
    msg = mod_pow(x, d, n)
    return msg

print("RSA message decrypted", RSA(1234567))
#Первообразный корень (генератор)
def primitive_root(modulo):
    required_set = set(num for num in range (1, modulo) if gcd(num, modulo) == 1)
    for g in range(1, modulo):
        actual_set = set(pow(g, powers) % modulo for powers in range (1, modulo))
        if required_set == actual_set:
            return g


import hashlib



#алгоритм SRP
def SRP(pas):
    N = 1
    q = 1
    g = 1
    while not miller(N, 1):
        q = genPrime(50, 300, 1)
        N = 2*q + 1
        g = primitive_root(N)
    salt = random.getrandbits(3)
    x = int(hashlib.sha256((str(salt)+pas).encode('utf-8')).hexdigest(), 16)
    v = mod_pow(g,x,N)
    a = random.randint(2,100)
    A = mod_pow(g,a,N)
    if A!=0:
        b = random.randint(2,100)
        k = 3
        B = (k*v + mod_pow(g,b,N) )%N
        if B!=0:
            u = int(hashlib.sha256(hex(A+B).encode('utf-8')).hexdigest(), 16)
            if u!=0:
                S1 = mod_pow((B - k * mod_pow(g,x,N) ) , (a + u * x), N)
                K1 = int(hashlib.sha256(hex(S1).encode('utf-8')).hexdigest(), 16)
                S2 = mod_pow((A * (mod_pow(v,u,N))), b, N)
                K2 = int(hashlib.sha256(hex(S2).encode('utf-8')).hexdigest(), 16)
                if K1==K2:
                    return "success", K1

print("SRP", SRP('33123123'))