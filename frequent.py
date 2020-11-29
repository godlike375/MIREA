import os




def isRus(ch): #проверка на русскую букву
    return (ord("а") <= ord(ch) <= ord("я")) or (ord("А") <= ord(ch) <= ord("Я"))

def count_freq(lines, freq): #подсчет частот из переданных строк
    for i in range(len(lines)):
        for let in lines[i].lower():
            if let.isalpha():
                if let in freq:
                    if isRus(let):
                        freq[let] = freq[let]+1
                else:
                    if isRus(let):
                        freq[let] = 1

def Cesar(line, key): #подсчет частот из переданных строк
        nl = []
        for ch in line:
            if isRus(ch):
                o = ord(ch) + key
                if not isRus(chr(o)):
                    if ch.isupper():
                        if o - ord("A") < 0:
                            o = ord("Я") + o - ord("A")
                        elif o - ord("Я") > 0:
                            o = ord("А") + o - ord("Я")
                    else:
                        if o - ord("а") < 0:
                            o = ord("я") + o - ord("а")
                        elif o - ord("я") > 0:
                            o = ord("а") + o - ord("я")
                nl.append(chr(o))
            else:
                nl.append(ch)
        return ''.join(nl)

def read_books(dir): #считать книги из дирректории и подсчитать их суммарные частоты
    books = []
    for file in os.listdir(dir):
        if os.path.isfile(dir + file):
            with open(dir + file, 'r', encoding='utf-8') as f:
                books.append(f.readlines())
    orig_freq = {}

    for i in range(len(books)):
        for j in range(len(books[i])):
            books[i][j] = books[i][j].replace('ё', 'е')
        count_freq(books[i], orig_freq)
        return orig_freq

def frequent_decode(origf, newf): #составить карту для конвертации между 2мя картами частот
    convert = {}
    for i in range(len(newf)):
        #print(origf[i][0], newf[i][0])
        convert[newf[i][0]] = origf[i][0]
    return convert

def analysis(conv, line): #дешифровать строки по карте конвертации
    nl = []
    for i in line:
        if i.lower() in conv:
            if i.isupper():
                nl.append(conv[i.lower()].upper())
            else:
                nl.append(conv[i])
        else:
            nl.append(i)
    return ''.join(nl)

def crypto(name, cdir, key): #зашифровать строки цезарем и записать в файл
    with open(cdir + name +'.txt', 'r', encoding='utf-8') as f:
        lines = f.readlines()
    for i in range(len(lines)):
        lines[i] = Cesar(lines[i], key)

    with open(cdir + name+' шифр.txt', 'w+', encoding='utf-8') as f:
        f.writelines(lines)

def decrypto(name, conv, lines, cdir): #расшифровать строки частотным анализом и записать в файл
    for i in range(len(lines)):
        lines[i] = analysis(conv, lines[i])
    with open(cdir + name+' анализ.txt', 'w+', encoding='utf-8') as f:
        f.writelines(lines)




def main():
    dir = r'C:\Users\godlike375\Downloads\война и мир\\' # директория для глав
    cdir = r'C:\Users\godlike375\Downloads\война и мир\цезарь\\' #директория для зашифрованных
    name = 'глава 4'
    key = 5
    crypto(name, cdir, key) #зашифровать определенную главу

    orig_freq = read_books(dir) #считать все книги из каталога и подсчитать их частоты монограмм

    orig_sorted = sorted(orig_freq.items(), key=lambda x: x[1], reverse=True) #сортировка по убыванию частот
    lines = [] #строчки из зашифрованной книги
    new_freq = {} #частоты зашифрованной книги
    with open(cdir + name+' шифр.txt', 'r', encoding='utf-8') as f:
        lines = f.readlines()

    count_freq(lines, new_freq) #считаем частоты зашифрованной книги
    new_sorted = sorted(new_freq.items(), key=lambda x: x[1], reverse=True) #сортируем по убыванию частоты

    conv = frequent_decode(orig_sorted, new_sorted) #сопоставляем 2 таблицы частот и получаем таблицу перекодировки

    decrypto(name, conv, lines, cdir) #декодируем строчки и записываем результаты в файл






main()