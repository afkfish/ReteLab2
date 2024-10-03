# Statisztikai alapfogalmak és adatelemzés

## 1. Alapvető statisztikai fogalmak

### Középértékek
- **Átlag (Mean)**: 
  - Az adatpontok összege osztva az adatpontok számával
  - Érzékeny a kiugró értékekre
  - Képlete: x̄ = (∑x_i)/n

- **Medián**: 
  - A sorba rendezett adatok középső értéke
  - Kevésbé érzékeny a kiugró értékekre
  - Páros számú adat esetén a két középső érték átlaga

- **Percentilis**:
  - Az adatok egy meghatározott százaléka ez alatt az érték alatt található
  - Például: 
    - 25. percentilis (Q1): az adatok 25%-a kisebb ennél
    - 50. percentilis: megegyezik a mediánnal
    - 75. percentilis (Q3): az adatok 75%-a kisebb ennél

## 2. Adatelemzés alapfogalmai

### Adatkeret (DataFrame)
- Táblázatos adatszerkezet
- Sorok: megfigyelések/esetek
- Oszlopok: változók/tulajdonságok

### Változók típusai
1. **Kvalitatív (Kategorikus)**
   - Nominális: nincs természetes sorrend (pl. szín, nem)
   - Ordinális: természetes sorrend van (pl. iskolai végzettség)

2. **Kvantitatív (Numerikus)**
   - Diszkrét: megszámlálható értékek (pl. gyerekek száma)
   - Folytonos: bármilyen érték lehet egy tartományban (pl. magasság, súly)

## 3. Vizualizációs eszközök

### Oszlopdiagram (Bar Chart)
- **Használat**: Kategorikus változók gyakoriságának ábrázolása
- **Értelmezés**: Az oszlopok magassága az előfordulás gyakoriságát mutatja
- **Alkalmazás**: Pl. termékek eladási statisztikái

### Hisztogram
- **Használat**: Folytonos változók eloszlásának vizsgálata
- **Értelmezés**: Az oszlopok szélessége az osztályközöket, magassága a gyakoriságot mutatja
- **Alkalmazás**: Pl. dolgozatok pontszámainak eloszlása

### Szórásdiagram (Scatter Plot)
- **Használat**: Két numerikus változó kapcsolatának vizsgálata
- **Értelmezés**: Pontok elhelyezkedése mutatja a kapcsolat erősségét és irányát
- **Alkalmazás**: Pl. magasság és súly összefüggése

### Dobozdiagram (Box Plot)
- **Használat**: Numerikus változó eloszlásának vizsgálata
- **Értelmezés**: 
  - Doboz: Q1-Q3 (interkvartilis tartomány)
  - Vonal a dobozban: medián
  - Bajuszok: minimum és maximum (kivéve kiugró értékek)
  - Pontok: kiugró értékek
- **Alkalmazás**: Pl. fizetések eloszlásának összehasonlítása különböző osztályokon

### Mozaik Plot
- **Használat**: Két vagy több kategorikus változó kapcsolatának vizsgálata
- **Értelmezés**: A területek mérete arányos a gyakoriságokkal
- **Alkalmazás**: Pl. nem és végzettség kapcsolatának vizsgálata

## 4. Lineáris regresszió

### Cél
- Két változó közötti lineáris kapcsolat modellezése
- Függő változó értékének előrejelzése a független változó alapján

### Alkalmazás
- Összefüggések feltárása
- Predikciók készítése
- Például:
  - Lakásár becslése alapterület alapján
  - Értékesítés előrejelzése marketing költségek alapján

### Értelmezés
- Az egyenes egyenlete: y = mx + b
  - m: meredekség (független változó hatása)
  - b: tengelymetszet (kezdeti érték)
- R²: illeszkedés jósága (0-1 között)
  - 0: nincs lineáris kapcsolat
  - 1: tökéletes lineáris kapcsolat
