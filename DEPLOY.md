## Deploymentplan

Die drei nachfolgenden Docker-Compose Dateien werden in separaten Fenstern ausgeführt um einen besseren Überblick darüber zu behalten, welche Komponenten welche Ausgaben macht. Zudem könnten so die Komponenten auch auf unterschiedlichen Computern o.Ä. ausgeführt werden.

Es werden nacheinander, jeweils in einem separatem Terminal, folgende Befehle ausgeführt:

#### Starten der Sensoren
Navigation zu `src/Sensordaten/` und den folgenden Befehl ausführen:

>docker-compose up  
>docker-compose start

#### Starten der Wetterstationen
Navigation zu `src/Wetterstation/` und den folgenden Befehl ausführen:

>docker-compose up  
>docker-compose start

#### Starten der Wetterdienste
Navigation zu `src/Wetterservice/` und den folgenden Befehl ausführen:

>docker-compose up  
>docker-compose start

&nbsp;

[Detailliertere Erklärung](README.md) aller Dateien und Kommunikation zwischen den einzelnen Komponenten.