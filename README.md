# OVERDRIVE USING GREEDY ALGORITHM
Memanfaatkan strategi greedy untuk memenangkan permainan overdrive. Program dibuat dalam rangka pemenuhan tugas besar IF2211 Strategi Algoritma
  
## Requirements
1. [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), JDK 8 recommended and don't forget to set the path (environment variable).
2. [IntelliJ IDEA](https://www.jetbrains.com/idea/)
3. [Node JS](https://nodejs.org/en/download/)

## How To Build
1. Download the [starter-pack](https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4) for this program
2. Clone this repository
3. Delete the existing **src** folder in **starter-bots/java**
4. Copy the the **src** folder from this repository, then paste it inside the **starter-bots/java** folder in the starter pack   
5. Open java folder with Intellij IDEA.
6. Open up the "Maven Projects" tab on the right side of the screen. From here go to the  **"java-sample-bot > Lifecycle** group and double-click **"Install"**. This  will create a .jar file in the folder called **target**. The file will be called "java-sample-bot-jar-with-dependencies.jar".

## How To Run
1. Update game-runner-config.json. Change "player-a" to "./starter-bots/java".
2. Go back to starter-pack folder.
3. Click run.bat
4. Visualize using the [community visualizer](https://github.com/Affuta/overdrive-round-runner)



Pada permainan Overdrive ini terdapat beberapa algoritma-algoritma greedy yang memaksimalkan kemungkinan kita untuk menang. Algoritma - algoritma ini memiliki kelebihan dan kekurangannya masing-masing tergantung pada jenis track dan musuh yang kita dapat. Di antara alternatif  algoritma-algoritma greedy yang sudah disampaikan pada sub bab 3.2, Algoritma Greedy by available command, greedy by damage, dan greedy by obstacle lah yang paling efektif dalam kondisi maksimumnya masing-masing. 
Pada akhirnya algoritma greedy yang kami pakai merupakan gabungan dari ketiga algoritma greedy tersebut. Kami menamai algoritma greedy final kami dengan greedy by speed. Untuk permulaan kami melakukan greedy by fix untuk mengecek apakah mobil butuh diperbaiki. Selanjutnya apabila mobil tidak perlu diperbaiki maka akan menggunakan greedy by available command yang dicampur dengan greedy by obstacle untuk pertimbangan pindah lane-nya.
Kami tidak menggunakan greedy by offensive attack karena algoritma tersebut terlalu bergantung pada adanya power up offense yang terlalu acak. Kami juga tidak menggunakan algoritma greedy by enemy position karena terlalu bergantung pada strategi apa yang musuh gunakan. Kami juga tidak menggunakan greedy by score karena kompleksitas waktunya paling tinggi dan juga tidak memaksimalkan efek dari tiap power up.

## Greedy by Speed
1.	Mendahulukan FIX terlebih dahulu jika mobil terlalu rusak (damage >= 5)
2.	Jika mobil berhenti (speed == 0 ), Dahulukan ACCELERATE
3.	Jika tidak ada obstacle di depan mobil dan kecepatan mobil kurang dari max speed, ACCELERATE
4.	Jika damage mobil lebih besar dari 2 dan bobot lane tempat mobil berada atau bobot lane tetangga ada yang <= 0, Lakukan FIX
5.	Jika BOOST tersedia,  Lakukan BOOST
6.	Jika memiliki power up Lizard, gunakan LIZARD
7.	Jika di depan banyak obstacle pindah jalur ke lane yang bobotnya lebih rendah
8.	Jika EMP tersedia, gunakan EMP
9.	Jika TWEET tersedia, gunakan TWEET
10.	Jika OIL tersedia, gunakan OIL
11.	Jika semua kondisi diatas tidak dipenuhi, gunakan ACCELERATE

#
## By apaan yak Team
1.	Nadia Mareta Putri Leiden		(13520007)
2. 	Daniel Salim				(13520008)
3.	Raden Haryosatyo Wisjnunandono		(13520070)
