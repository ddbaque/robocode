# 🤖 Robocode 🤖 

## Índex
- [⚙️Entorn de Treball ⚙️](#⚙️-entorn-de-treball-⚙️)
  - [Editor de codi](#editor-de-codi)
- [😳 TimidínRobot 😳](#😳-timidínrobot-😳)
     - [Plantejament i organització del codi](#plantejament-i-organització-del-codi)
     - [Detalls de la implementació](#detalls-de-la-implementació)
        - [TimidinRobot.java](#timidinrobot.java)
        - [State.java](#state.java)
        - [Detection.java](#detection.java)
        - [MoveToCorner.java](#movetocorner.java)
        - [Attack.java](#attack.java)
     - [Explicació dels càlculs](#explicació-dels-càlculs)
        - [Normalitzar angles](#normalitzar-angles)
        - [Càlcul de les coordenades de l’enemic](#càlcul-de-les-coordenades-del-contrari)
        - [Càlcul de la distància de l’enemic a cada cantonada](#càlcul-de-la-distància-del-contrari-a-cada-cantonada)
        - [Càlcul i normalització de l’angle de gir](#càlcul-i-normalització-angle-de-gir)
        - [Girar canó cap a l’enemic](#girar-canó-cap-al-contrari)
        - [Càlcul de la potència del tret](#càlcul-de-la-potència-del-tret)
        - [Decisió de la direcció de gir basada en enemyBearing](#decisió-de-la-direcció-de-gir-basada-en-enemybearing)
- [🤝 FollowTheLeaderTeam 🤝](#🤝-followtheleaderteam-🤝)

## ⚙️ Entorn de Treball ⚙️

### Editor de codi

En nuestro equipo decidimos no utilizar el **IDE NetBeans** para programar en Java, ya que no consideramos necesario el uso de un IDE completo para este proyecto. Al tratarse de un entorno en Linux, optamos por editores de código, que eran más que suficientes para nuestras necesidades, **Visual Studio Code** y **NeoVim**. Ambos editores ofrecen flexibilidad y la posibilidad de configurar el entorno de desarrollo a medida.

Para obtener soporte de autocompletado y vinculación con la librería de Robocode, simplemente debíamos dirigirnos al directorio donde habíamos instalado Robocode. En nuestro caso, lo descargamos en `/home/<usuario>/robocode`, y dentro del directorio `robocode/libs/robocode.jar` encontramos el archivo de la librería necesario para proporcionar autocompletado y soporte para Robocode en los editores.

Sin embargo, no pudimos resolver la importación automática de robots mediante el método sugerido en el PowerPoint de Atenea. Para solucionar este inconveniente, creamos un sencillo script llamado **compile_robot.sh**, que se encargaba de compilar el código Java de los robots y copiarlos directamente al directorio de Robocode. Esto nos permitió importar los robots y tenerlos listos para usar en el juego de manera rápida.


`compile_robot.sh:`
``` bash 
javac -cp $1/libs/robocode.jar $2/*.java
sudo cp -r $2 $1/robots
```

Con este enfoque, pudimos compilar y mover nuestros robots a la carpeta `/home/<usuario>/robocode/robots`, logrando que estuvieran listos para ser utilizados en Robocode sin problemas adicionales.

### Gestió del codi

## 😳 TimidínRobot 😳

### Plantejament i organització del codi

El robot **TimidínRobot** s'ha desenvolupat fent servir el patró de màquina d'estats (state design pattern), i això permet organitzar el comportament de Timidín en diferents estats que gestionen els esdeveniments de manera específica. Els estats definits corresponen a les fases proporcionades a l'enunciat.

- **Estat 0 - Detection:** Timidín escaneja el camp de batalla buscant enemics amb el radar donant una volta de 360 graus. Quan detecta un enemic per primer cop, calcula la cantonada més llunyana de la posició de l'enemic i canvia al següent estat (`MoveToCorner`).
- **Estat 1 - MoveToCorner:** Timidín es mou en línia recta cap a la cantonada calculada a l'estat anterior. Si es troba amb cap obstacle, dispara al robot detectat i l'esquiva. Quan arriba a la cantonada objectiu passa al següent estat (`Attack`).
- **Estat 2 - Attack:** Timidín escaneja cercant enemics i dispara quan en detecta un, ajustant la potència del tret segons la distància de l’enemic.

El codi s’organitza en diferents classes per separar les responsabilitats: 

- **TimidinRobot.java:** És la classe principal del robot. Aquesta actua com un controlador que delega les accions als estats corresponents. Manté l’estat actual y s’encarrega de la transició entre estats.
- **State.java:** Defineix el comportament comú dels diferents estats.
- **Detection.java, MoveToCorner.java i Attack.java:** Són classes que implementen la interfície `State`, representant els estats 0, 1 i 2 respectivament.

---

### Detalls de la implementació

#### TimidinRobot.java

- **Atributs:**
    - `state`: representa l'estat actual del robot.
    - `lastScannedRobot`: emmagatzema l'últim esdeveniment de detecció d'un robot enemic.
    - `targetX`, `targetY`: coordenades de la cantonada objectiu.
- **Mètodes:**
    - `setState(State newState)`: canvia l'estat del robot a l'especificat.
    - `normalizeBearing(double angle)`: normalitza un angle perquè estigui en el rang de -180 a 180 graus.
    - `run()`: mètode principal que configura el color del robot, estableix l'estat inicial, i executa el cicle continu on anomena al mètode `run()` de l’estat actual.
    - Mètodes d'esdeveniments  (`onScannedRobot`, `onHitRobot`, `onHitWall`): deleguen el maneig de l'esdeveniment a l'estat actual.

#### State.java

**Mètodes:**

- `run()`: mètode que conté la lògica principal de l'estat.
- `onScannedRobot(ScannedRobotEvent e)`: s'executa quan el radar detecta un robot enemic.
- `onHitRobot(HitRobotEvent e)`: s'executa quan el robot col·lideix amb un altre robot.
- `onHitWall(HitWallEvent e)`: s'executa quan el robot colpeja una paret.

#### Detection.java

- **Atributs:**
    - `robot`: referència al robot per a accedir als seus mètodes i atributs.
- **Mètodes:**
    - `run()`: gira el radar contínuament (360 graus) per a escanejar el camp de batalla.
    - `onScannedRobot(ScannedRobotEvent e)`:  quan es detecta un robot enemic, calcula la cantonada més llunyana i canvia a l'estat `MoveToCorner`.
    - `calculateFurthestCorner(ScannedRobotEvent e)`: determina quina de les quatre cantonades del camp de batalla està més allunyada del robot enemic detectat.
    - `onHitRobot` y `onHitWall`: no hem implementat accions en aquest estat.

#### MoveToCorner.java

- **Atributs:**
    - `robot`: referència al robot per a accedir als seus mètodes i atributs.
    - `lastHitRobotId`, `hitRobotCount`: ajuden a gestionar col·lisions repetides amb el mateix robot.
    - `random`: generador de números aleatoris per a variar la direcció per esquivar.
    - `angleOffset`: angle utilitzat per a evitar obstacles.
- **Mètodes implementats:**
    - `run()`: calcula la direcció cap a la cantonada objectiu i mou el robot en línia recta cap a allí. Si el robot està a menys de 40 unitats de distància de l'objectiu, canvia a l'estat `Attack`.
    - `onHitRobot(HitRobotEvent e)`: dispara i esquiva a altres robots en col·lisionar amb ells. Si es colpeja el mateix robot repetidament, es realitza un gir brusc per a intentar escapar.
    - `onHitWall(HitWallEvent e)`: realitza ajustos al rumb quan es detecta una col·lisió amb la paret. Inclou lògica per a sortir d'una cantonada si està atrapat.
    - `isSurrounded()`: determina si el robot està envoltat d'enemics, utilitzat per a decidir si realitzar un gir brusc.

#### Attack.java

- **Atributs:**
    - `robot`: referència al robot per a accedir als seus mètodes i atributs.
- **Mètodes implementats:**
    - `run()`: gira el radar per a buscar enemics. Si hi ha un enemic emmagatzemat en `lastScannedRobot`, intenta disparar.
    - `onScannedRobot(ScannedRobotEvent e)`: emmagatzema l'esdeveniment de detecció i crida a `aimAndFire()` per a atacar.
    - `aimAndFire(ScannedRobotEvent e)`: ajusta l'angle del canó per a apuntar cap a l'enemic i dispara amb una potència calculada en funció de la distància.
    - `onHitRobot` y `onHitWall`: no hem implementat accions en aquest estat.

---

### Explicació dels càlculs

#### Normalitzar angles

La funció `normalizeBearing` (a la classe TimidinRobot.java) serveix per a ajustar un angle perquè estigui dins del rang de -180 a 180 graus amb l’objectiu d’indicar la direcció més curta de gir cap a l'objectiu (ja que tenint en compte els 360 graus es pot arribar als objectius girant tat a l’esquerra com a la dreta, només que una de les opcions és més curta i eficient).

```java
public double normalizeBearing(double angle) {
    while (angle > 180) angle -= 360;
    while (angle < -180) angle += 360;
    return angle;
  }
```

Per normalitzar l’angle li hem restat 360 graus repetidament fins que ha quedat dins del rang desitjat en el cas de que l’angle sigui superior a 180. Si l'angle és inferior a -180 graus li hem sumat 360 graus repetidament fins que ha quedat dins del rang desitjat.

#### Càlcul de les coordenades del contrari

Dins de la funció `calculateFurthestCorner`  (a la classe Detection.java).

```java
double enemyX = robot.getX() + e.getDistance() 
					    	* Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
double enemyY = robot.getY() + e.getDistance() 
								* Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
```

- La coordenada X de l'enemic (`enemyX` ) s'obté sumant a la posició actual del robot (`robot.getX()`) la projecció de la distància ( `e.getDistance()`) en l'eix X. Primer, es calcula l'angle absolut cap a l'enemic sumant el heading (direcció absoluta del robot) amb el bearing (angle relatiu a la posició del robot respecte a l'enemic). Aquest angle representa la direcció del robot en el camp de batalla combinada amb la direcció relativa cap a l'enemic. L'angle es converteix a radiants fent servir la funció `Math.toRadians()` er poder utilitzar les funcions trigonomètriques. La projecció horitzontal es calcula amb `Math.sin()`, que dona el component horitzontal de la distància en la direcció de l'angle.

$$
enemyX= possicióRobotX +  distànciaEnemicX ×sin(radians(heading+bearing))
$$

- La coordenada Y de l'enemic (`enemyY`) es calcula de manera similar, sumant a la posició actual del robot (`robot.getY()`) la projecció de la distància en l'eix Y.  Utilitzant l'angle absolut ja calculat, es determina la projecció vertical fent servir `Math.cos()`, que dona el component vertical de la distància  en la direcció de l'angle.

$$
enemyY= possicióRobotY +  distànciaEnemicY ×cos(radians(heading+bearing))
$$

#### Càlcul de la distància del contrari a cada cantonada

Dins de la funció `calculateFurthestCorner`  (a la classe Detection.java).

1. Decisió dels valors de les coordenades de cada cantonada:
    
    ```java
    double offset = 15;
    double[] cornersX = {offset, offset, battlefieldWidth - offset
    										    , battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset
    								    , offset, battlefieldHeight - offset};
    ```
    
    - `(offset, offset)`  per a la cantonada inferior esquerra.
    - `(offset, battlefieldHeight - offset)`  per a la cantonada superior esquerra.
    - `(battlefieldWidth - offset, offset)` per a la cantonada inferior dreta.
    - `(battlefieldWidth - offset, battlefieldHeight - offset)` per a la cantonada superior dreta.
    
    Hem definit un petit marge de seguretat (`offset`)  per a evitar acostar-se massa als límits del camp de batalla.
    
2. Càlcul de la distància de l’enemic a cada cantonada
    
    ```java
    double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
    ```
    
    Per mesurar la distància de l’enemic a cada cantonada hem fet servir el càlcul de la distància euclidiana, que és la longitud del segment de línia recta que connecta dos punts. En el nostre cas els dos punts són les coordenades a on es troba l’enemic `(enemyX, enemyY)` i les coordenades de cada cantonada (al punt iii. ). Es calula: 
    
    $$
    distància= \sqrt{(enemicX−cantonadaX)^2+(enemicY−cantonadaY)^2}
    $$
    
    Per fer aquest càlcul, però, hem fet servir la funció `Math.hypot` .
    

#### Càlcul de la distància i l’ angle a la cantonada objectiu

Dins de la funció `run()`  (a la classe MoveToCorner.java).

##### Càlcul de la distància

```java
double dx = robot.targetX - robot.getX();
double dy = robot.targetY - robot.getY();
```

Per a determinar la direcció de l'objectiu, hem les diferències entre la possició de la cantonada i del robot en els eixos X e Y:

$$
distànciaX=posicióCantonadaX−posicióRobotX
$$

$$
distànciaY=posicióCantonadaY−posicióRobotY
$$

Així obtenim els components del vector de desplaçament des de la posició actual del robot fins a l'objectiu (distànciaX,distànciaY), al codi `(dx,dy)`.

El vector desplaçament és (distànciaX,distànciaY), al codi `(dx,dy)`, i representa la diferència en les coordenades X i Y entre la posició actual del robot i la posició de l'objectiu.

##### Càlcul de l’angle

```java
double angleToTarget = Math.toDegrees(Math.atan2(dx, dy));
```

Per a trobar l'angle que forma el vector `(dx,dy)` respecte a  la direcció del robot (l'eix Y), hem de calcular l'angle θ que compleix la relació trigonomètrica de la tangent, i fem servir l’arctangent:

$$
tan(θ)= \frac{dx}{dy} \rightarrow θ= tan^{-1}(\frac{dx}{dy})
$$

Si simplement apliquem l’arctangent ens donarà l'angle corresponent, però en l'interval restringit de −π/2 a π/2 en radiants ( −90 a 90 en graus). Això vol dir que el resultat només serà correcte si l’angle es troba al primer o al quart quadrant, però si el vector està en el segon o tercer quadrant, l'angle no representarà la direcció real de l'objectiu. Per a obtenir l'angle correcte en qualsevol quadrant, hem ajustat manualment el resultat de la funció arctangent:

- Si dx>0 i dy>0, l'angle està en el primer quadrant.
- Si dx>0 i dy<0, l'angle està en el quart quadrant.
- Si dx<0 i dy>0, l'angle es troba en el segon quadrant.
- Si dx<0 i dy<0, l'angle està en el tercer quadrant.

Depenent del quadrant en el qual es trobi el vector, s’ha de fer un dels següents ajustos:

- Si el vector està en el segon o tercer quadrant (dx<0) , sumem 180 graus per a ajustar l'angle.
- Si està en el quart quadrant (dx>0 i dy<0), sumem 360 graus per a obtenir un angle positiu.

La funció `Math.atan2(dx, dy)` té en compte els signes de `dx` i `dy`, per tant retorna un angle en el rang complet de −π a π, vol dir que cobreix tots els quadrants. Això evita la necessitat d'ajustos addicionals. Fem servir també `Math.toDegrees` per fer la conversió de radians a graus.

#### Càlcul i normalització angle de gir

Dins de la funció `run()`  (a la classe MoveToCorner.java).

```java
double turnAngle = robot.normalizeBearing(angleToTarget - robot.getHeading());
```

L'angle de gir necessari per a alinear el robot cap a l'objectiu es calcula restant l'angle actual del robot (heading) de l'angle cap a l'objectiu(`angleToTarget`, dit θ a l’explicació anterior):

$$
angleGir= θ - heading
$$

Es fa servir la funció `normalizeBearing` (explicada al punt 3.1) per a ajustar un angle perquè estigui dins del rang de -180 a 180 graus.

#### Girar canó cap al contrari

Dins de la funció `onHitRobot()`  (a la classe MoveToCorner.java), també dins de la funció `aimAndFire()`  (a la classe Attack.java).

```java
double gunTurnAngle = robot.getHeading() + enemyBearing - robot.getGunHeading();
```

L’angle per girar el canó cap a l’enemic l’hem calculat sumant la direcció actual del robot (heading) i l’angle relatiu al robot a on es troba l’enemic (bearing) i després restant  la direcció actual a la que està apuntant el canó del robot (gun heading).

$$
angleGirCanó=headingRobot+bearing-headingCanó
$$

Després s’ha de fer servir la funció `normalizeBearing` (explicada al punt 3.1) per a ajustar un angle perquè estigui dins del rang de -180 a 180 graus quan fem girar el canó fent servir la funció `setTurnGunRight`.

#### Decisió de la direcció de gir basada en enemybearing

Dins de la funció `onHitRobot()`  (a la classe MoveToCorner.java).

```java
 if (enemyBearing > 0) {
    robot.setTurnLeft(angleOffset + random.nextInt(45)); 
  } else {
    robot.setTurnRight(angleOffset + random.nextInt(45));
  }
```

Si e l’`enemyBearing` és positiu, el robot gira a l'esquerra amb un angle aleatori, si és negatiu, gira a la dreta. L'angle es basa en un valor fix (`angleOffset`) sumat a un número aleatori entre 0 i 45 graus. 

El motiu per el que escollim segons el valor de l’`enemyBearing`  es deu a que permet al robot reaccionar basant-se en la posició relativa de l'enemic. Quan és positiu, significa que l'enemic està a la dreta del robot. En conseqüència, el robot girarà cap a l'esquerra per a desviar la seva trajectòria. Si és negatiu, l'enemic està a l'esquerra, llavors el robot girarà cap a la dreta. Aquest mecanisme permet al robot reaccionar basant-se en la posició relativa de l'enemic.

La decisió de gir segons l’`enemyBearing`  també es fa servir a la funció `onHitWall()` si el robot no es troba a una cantonada.

#### Càlcul de la potència del tret

Dins de la funció `aimAndFire()`  (a la classe Attack.java).

```java
double firePower = Math.min(400 / e.getDistance(), 3);
```

Ens hem basat en la distància a la que es troba el robot de l’enemic al que vol disparar perquè així s’equilibra l'ús eficient de l'energia del robot i maximitza el mal a mesura que l'enemic s'acosta, sense malgastar energia quan l'enemic està lluny (ja que la probabilitat de que no encertem el tret augmenta proporcionalment amb la distància que els separa).

Dividir un nombre qualsevol (400 en el nostre cas) entre la distància a la que es troba l’enemic determina  que com més a prop estigui l'enemic, major serà la potència del tret. A mesura que l'enemic s'acosta, el valor de `getDistance` disminueix, la qual cosa fa que el valor de `firePower` augmenti.

 La funció `Math.min` garanteix que la potència màxima del tret no excedeixi 3 unitats (el valor màxim permès a Robocode). 

---

## 🤝 FollowTheLeaderTeam 🤝

### 1. Plantejament i organització del codi

### 2. Detalls de la implementació

### 3. Explicació dels càlculs

#### 3.1 Trobar la cantonada més llunyana de l’enemic detectat

La funció `calculateFurthestCorner`  (a la classe Detection.java ) serveix per determinar quina de les quatre cantonades del camp de batalla està més allunyada del robot enemic detectat.

```java
 private void calculateFurthestCorner(ScannedRobotEvent e) {
 
    double enemyX = robot.getX() + e.getDistance() 
					    	* Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
		double enemyY = robot.getY() + e.getDistance() 
										* Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
		
		double battlefieldWidth = robot.getBattleFieldWidth();
		double battlefieldHeight = robot.getBattleFieldHeight();
		double offset = 15;

		double[] cornersX = {offset, offset, battlefieldWidth - offset
										    , battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset
										    , offset, battlefieldHeight - offset};

    double maxDistance = -1;
    for (int i = 0; i < cornersX.length; i++) {
      double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
      if (distance > maxDistance) {
        maxDistance = distance;
        robot.targetX = cornersX[i];  /
        robot.targetY = cornersY[i];  
      }
    }
  }
```

1. Càlcul de les coordenades de l’enemic 
    
    ```java
    double enemyX = robot.getX() + e.getDistance() 
    					    	* Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
    double enemyY = robot.getY() + e.getDistance() 
    								* Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
    ```
    
    - La coordenada X de l'enemic (`enemyX` ) s'obté sumant a la posició actual del robot (`robot.getX()`) la projecció de la distància ( `e.getDistance()`) en l'eix X. Primer, es calcula l'angle absolut cap a l'enemic sumant el heading (direcció absoluta del robot) amb el bearing (angle relatiu a la posició del robot respecte a l'enemic). Aquest angle representa la direcció del robot en el camp de batalla combinada amb la direcció relativa cap a l'enemic. L'angle es converteix a radiants fent servir la funció `Math.toRadians()` er poder utilitzar les funcions trigonomètriques. La projecció horitzontal es calcula amb `Math.sin()`, que dona el component horitzontal de la distància en la direcció de l'angle.
    
    $$
    enemyX= possicióRobot +  distànciaEnemic ×sin(radians(heading+bearing))
    $$
    
    - La coordenada Y de l'enemic (`enemyY`) es calcula de manera similar, sumant a la posició actual del robot (`robot.getY()`) la projecció de la distància en l'eix Y.  Utilitzant l'angle absolut ja calculat, es determina la projecció vertical fent servir `Math.cos()`, que dona el component vertical de la distància  en la direcció de l'angle.
    
    $$
    enemyX= possicióRobot +  distànciaEnemic ×cos(radians(heading+bearing))
    $$
    
2. Obtenir la mida del camp de batalla
    
    ```java
    double battlefieldWidth = robot.getBattleFieldWidth();
    double battlefieldHeight = robot.getBattleFieldHeight();
    double offset = 15;
    ```
    
    - S'extreu l'amplada l’alçada del camp de batalla fent servir les funcions `battlefieldWidth(` i `battlefieldHeight()` respectivament.
    - Es defineix un petit marge de seguretat (`offset`)  per a evitar acostar-se massa als límits del camp de batalla.
3. Definició de les coordenades de les cantonades
    
    ```java
    double offset = 15;
    double[] cornersX = {offset, offset, battlefieldWidth - offset
    										    , battlefieldWidth - offset};
    double[] cornersY = {offset, battlefieldHeight - offset
    								    , offset, battlefieldHeight - offset};
    ```
    
    Les coordenades que hem decidit per les cantonades són: 
    
    - `(offset, offset)`  per a la cantonada inferior esquerra.
    - `(offset, battlefieldHeight - offset)`  per a la cantonada superior esquerra.
    - `(battlefieldWidth - offset, offset)` per a la cantonada inferior dreta.
    - `(battlefieldWidth - offset, battlefieldHeight - offset)` per a la cantonada superior dreta.
4. Determinació de la cantonada més llunyana
    
    ```java
    double maxDistance = -1
    for (int i = 0; i < cornersX.length; i++) {
      double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
      if (distance > maxDistance) {
        maxDistance = distance;
        robot.targetX = cornersX[i]; 
        robot.targetY = cornersY[i];  
      }
    }
    ```
    
    1. Càlcul de la distància de l’enemic a cada cantonada
        
        ```java
        double distance = Math.hypot(enemyX - cornersX[i], enemyY - cornersY[i]);
        ```
        
        Per mesurar la distància de l’enemic a cada cantonada hem fet servir el càlcul de la distància euclidiana, que és la longitud del segment de línia recta que connecta dos punts. En el nostre cas els dos punts són les coordenades a on es troba l’enemic `(enemyX, enemyY)` i les coordenades de cada cantonada (al punt iii. ). Es calula: 
        
        $$
        distància= \sqrt{(enemicX−cantonadaX)^2+(enemicY−cantonadaY)^2}
        $$
        
        Per fer aquest càlcul, però, hem fet servir la funció `Math.hypot` .
        
    2. Comparació de distàncies per a trobar la cantonada més llunyana i actualització de la cantonada objectiu
        
        Comparem cada distància calculada amb la distància més gran registrada fins al moment (`maxDistance`). Si la distància calculada és major que `maxDistance`, significa que la cantonada actual està més lluny de l'enemic que qualsevol de les revisades anteriorment. Per tant, s'actualitza `maxDistance` amb aquesta nova distància i es registren les coordenades de la cantonada com les noves `robot.targetX` i `robot.targetY`.
