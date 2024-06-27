import time

def OnMult(m_ar, m_br):
    pha = [[1.0] * m_ar for _ in range(m_ar)]
    phb = [[i+1] * m_br for i in range(m_br)]
    phc = [[0.0] * m_ar for _ in range(m_ar)]

    start_time = time.time()

    for i in range(m_ar):
        for j in range(m_br):
            temp = 0
            for k in range(m_ar):
                temp += pha[i][k] * phb[k][j]
            phc[i][j] = temp

    end_time = time.time()
    print(f"Time: {end_time - start_time} seconds")

def OnMultLine(m_ar, m_br):
    pha = [[1.0] * m_ar for _ in range(m_ar)]
    phb = [[i+1] * m_br for i in range(m_br)]
    phc = [[0.0] * m_ar for _ in range(m_ar)]

    start_time = time.time()

    for i in range(m_ar):
        for k in range(m_ar):
            for j in range(m_br):    
                phc[i][j]  += pha[i][k] * phb[k][j]


    end_time = time.time()
    print(f"Time: {end_time - start_time} seconds")    