package za.co.entelect.challenge;

import jdk.internal.net.http.websocket.WebSocketImpl;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private int maxSpeed;


    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
        if (myCar.damage == 0){
            this.maxSpeed = 15;
        }
        else if (myCar.damage == 1){
            this.maxSpeed = 9;
        }
        else if (myCar.damage == 2){
            this.maxSpeed = 8;
        }
        else if (myCar.damage == 3){
            this.maxSpeed = 6;
        }
        else if (myCar.damage == 4){
            this.maxSpeed = 3;
        }
        else if (myCar.damage == 5){
            this.maxSpeed = 0;
        }

        directionList.add(-1);
        directionList.add(1);
    }

    // Strategi Utama
    public Command run() {



        int greedy_by_obstacle_res = GreedybyObstacle(myCar.position.lane, myCar.position.block, myCar.speed);

        //FOR DEBUGGING PURPOSES
        System.out.println(laneRisk(myCar.position.lane, myCar.position.block, myCar.speed));
        if (myCar.position.lane == 1){
            System.out.println("kanan: "+laneRisk(myCar.position.lane + 1, myCar.position.block, myCar.speed));
        }
        if (myCar.position.lane == 4){
            System.out.println("kiri: " + laneRisk(myCar.position.lane -1 , myCar.position.block, myCar.speed));
        }
        if (myCar.position.lane == 2 || myCar.position.lane == 3){
            System.out.println("kanan: "+laneRisk(myCar.position.lane + 1, myCar.position.block, myCar.speed));
            System.out.println("kiri: " +laneRisk(myCar.position.lane -1 , myCar.position.block, myCar.speed));
        }
        System.out.println(greedy_by_obstacle_res);


        //IF TOO MUCH DAMAGE FIX FIRST
        if (this.myCar.damage >= 5)  {
            return FIX;
        }
        //IF TOO SLOW ACCELERATE FIRST
        if (this.myCar.speed == 0){
            return ACCELERATE;
        }

        //IF NO OBSTACLE AHEAD AND SPEED < MAX SPEED ACCELERATE
        if (this.myCar.speed != maxSpeed && laneRisk(myCar.position.lane, myCar.position.block, SpeedAfterAccelerating(myCar)) <= 0){
            return ACCELERATE;
        }

        // FIX jika damage kelipatan 2 dan lane mobil atau lane tetangga ada yang bobotnya 0
        if (this.myCar.damage >= 2){
            if (myCar.position.lane == 1){
                if(laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) <= 0 || laneRisk(myCar.position.lane + 1, myCar.position.block, myCar.speed-1) <= 0){
                    return FIX;
                }
            }
            else if (myCar.position.lane == 4){
                if(laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) <= 0 || laneRisk(myCar.position.lane -1, myCar.position.block, myCar.speed-1) <= 0){
                    return FIX;
                }
            }
            else{
                if(laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) <= 0 || laneRisk(myCar.position.lane + 1, myCar.position.block, myCar.speed-1) <= 0 || laneRisk(myCar.position.lane - 1, myCar.position.block, myCar.speed-1) <= 0){
                    return FIX;
                }

            }
        }

        //IF BOOST AVAILABLE --> USE BOOST
        if (IsBoostAvailable()){
            return BOOST;
        }
        //IF LIZARD AVAILABLE --> USE LIZARD
        if (isLizardAvailable()){
            return LIZARD;
        }



        /** mengecek setiap bobot lane yang ada, dan menentukan apakah perlu pindah lane atau tidak **/
        if(greedy_by_obstacle_res != myCar.position.lane){
            if(myCar.position.lane == 1){
                return new ChangeLaneCommand(directionList.get(1));
            }else if(myCar.position.lane == 2){
                if(greedy_by_obstacle_res == 3){
                    return new ChangeLaneCommand(directionList.get(1));
                }else{
                    return new ChangeLaneCommand(directionList.get(0));
                }
            }else if(myCar.position.lane == 3){
                if(greedy_by_obstacle_res == 4){
                    return new ChangeLaneCommand(directionList.get(1));
                }else{
                    return new ChangeLaneCommand(directionList.get(0));
                }
            }else{
                return new ChangeLaneCommand(directionList.get(0));
            }
        }


        
        /* --- Strategi Pakai Powerup (dari yang paling diprioritaskan) --- */

        /* --- EMP --- */  /* Kalo lane sama, mobil kita ada di belakang mobil lawan, dan punya EMP, maka pakai EMP*/
        if (isEMPAvailable()) {
            return EMP;
        }

        if ((myCar.speed < this.maxSpeed) &&  laneRisk(myCar.position.lane, myCar.position.block, SpeedAfterAccelerating(myCar)) <= 0){
            return ACCELERATE;
        }

    // IF TWEET > 0 USE TWEET
        if (isTweetAvailable()){
            int enemyLane = opponent.position.lane;
            int enemyPos = opponent.position.block + SpeedAfterAccelerating(opponent) + 1;
            //DEPLOY TWEET AT ENEMY LANE, ENEMY BLOCK + ENEMY SPEED AFTER ACCELERATION + 1
            return new TweetCommand(enemyLane, enemyPos);
        }
// IF IN FRONT AND HAS OIL --> USE OIL
        if (isOilAvailable()){
            return OIL;
        }

        
        /* --- TWEET --- */
        /* if (!(isEMPAvailable())) {
            if (!(IsBoostAvailable())) {
                if (!(isLizardAvailable())) {
                    if (isTweetAvailable()) {
                        return TWEET;
                    }
                }
            }
        } */

        /* --- OIL --- */
        /* if (!(isEMPAvailable())) {
            if (!(IsBoostAvailable())) {
                if (!(isLizardAvailable())) {
                    if (!(isTweetAvailable())) {
                        if (isOilAvailable()) {
                            return OIL;
                        }
                    }
                }
            }
        } */

//        /** kalo lanenya sama dan mobil kita ada di depan, bisa buang oli **/
//        if(IsInSameLane(myCar.position.lane, opponent.position.lane)){
//            if(IsInFront(myCar.position.lane, opponent.position.lane, opponent.position.block, opponent.position.block)){
//                if (checkPowerUp(PowerUps.OIL, myCar.powerups)) {
//                    return OIL;
//                }
//            }
  //      }
       

        


//        else if (blocks.contains(Terrain.MUD)) {
//            int i = random.nextInt(directionList.size());
//            return new ChangeLaneCommand(directionList.get(i));
//        }
        return ACCELERATE;
    }

//    /**
//     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
//     * traversed at max speed.
//     **/
//    private List<Object> getBlocksInFront(int lane, int block) {
//        List<Lane[]> map = gameState.lanes;
//        List<Object> blocks = new ArrayList<>();
//        int startBlock = map.get(0)[0].position.block;
//
//        Lane[] laneList = map.get(lane - 1);
//        for (int i = max(block - startBlock, 0); i <= block - startBlock + maxSpeed; i++) {
//            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
//                break;
//            }
//
//            blocks.add(laneList[i].terrain);
//
//        }
//        return blocks;
//    }
    
    /** ngecek apakah satu lane atau engga **/
    
    private boolean IsInSameLane(int lane, int lane2){
        if(lane == lane2){
            return true;
        }
        return false;
    }

    /** ngecek apakah posisi mobil ada di depan bot **/
    private boolean IsInFront(int lane, int lane2, int block, int block2){
        boolean status = false;
        if(IsInSameLane(lane, lane2)){
            if(block > block2){
                status = true;
            }
        }
        return status;
    }

    private boolean inFrontOf(Car playerCar, Car opponentcar){
        boolean flag = false;
        if (playerCar.position.block > opponentcar.position.block){
            flag = true;
        }
        return flag;
    }


    private boolean IsBehind(int lane, int lane2, int block, int block2){
        boolean status = false;
        if(IsInSameLane(lane, lane2)){
            if(block < block2){
                status = true;
            }
        }
        return status;
    }

    private boolean isCarinBlock(Car car, int lane, int block){
        boolean flag = false;
        if (car.position.lane == lane && car.position.block == block){
            flag = true;
        }
        return flag;
    }

    private int SpeedAfterAccelerating(Car car){
        if (car.speed == 0 && car.damage < 5){
            return  3;
        }
        else if (car.speed == 3 && car.damage < 4){
            return 5;
        }
        else if (car.speed == 5 && car.damage < 4){
            return 6;
        }
        else if (car.speed == 6 && car.damage < 3){
            return  8;
        }
        else if (car.speed == 8 && car.damage < 2){
            return  9;
        }
        else{
            return car.speed;
        }

    }

    private boolean checkPowerUp(PowerUps check_power, PowerUps[] existing){
        for(PowerUps power: existing){
            if(power.equals(check_power)){
                return true;
            }
        }
        return false;
    }

    private int countPowerUp(PowerUps check_power, PowerUps[] existing){
        int counter = 0;
        for(PowerUps power: existing){
            if(power.equals(check_power)){
                counter += 1;
            }
        }
        return counter;
    }

    // Menghitung bobot lane dalam param speed block ke depan
    private int laneRisk(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int weight = 0;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            else if (laneList[i].terrain == Terrain.MUD || laneList[i].terrain == Terrain.OIL_SPILL){
                weight += 2;
            }
            else if (laneList[i].terrain == Terrain.WALL || isCarinBlock(opponent,  lane, i)){
                weight += 4;
            }
            else if (laneList[i].terrain == Terrain.BOOST || laneList[i].terrain == Terrain.LIZARD){
                weight -= 3;
            }
            else if (laneList[i].terrain == Terrain.TWEET && countPowerUp(PowerUps.TWEET, myCar.powerups) <3 ){
                weight -= 1;
            }
            else if (laneList[i].terrain == Terrain.OIL_POWER && myCar.position.block >= opponent.position.block && countPowerUp(PowerUps.OIL, myCar.powerups) <3){
                weight -= 1;
            }
            else if (laneList[i].terrain == Terrain.EMP && myCar.position.block <=  opponent.position.block && countPowerUp(PowerUps.EMP, myCar.powerups) <3){
                weight -= 1;
            }

        }
        return weight;
    }

    private int GreedybyObstacle(int lane, int block, int speed){
        int number;
        int current_lane = laneRisk(lane, block, speed);
        if(lane == 1){
            int store_score = laneRisk(lane+1, block, speed);
            if(current_lane <= store_score){
                number = lane;
            }else{
                number = lane + 1;
            }
        }else if(lane == 4){
            int store_score = laneRisk(lane-1, block, speed);
            if(current_lane <= store_score){
                number = lane;
            }else{
                number = lane - 1;
            }
        }else{
            int store_score = laneRisk(lane-1, block, speed);
            int store_score2 = laneRisk(lane + 1, block, speed);
            if(current_lane <= store_score && current_lane <= store_score2){
                number = lane;
            }else if(store_score <= store_score2){
                number = lane - 1;
            }else {
                number = lane + 1;
            }
        }
        return number;
    }

// Algoritma pemakaian powerup sesuai urutan prioritas 

//EMP akan dipakai ketika mobil kita dibelakang mobil musuh dan satu lane
    // Periksa ketersediaan EMP
    public boolean isEMPAvailable() {
        boolean flag = false;
        if (checkPowerUp(PowerUps.EMP, myCar.powerups)){
            if(IsBehind(myCar.position.lane, opponent.position.lane, myCar.position.block, opponent.position.block)){
                flag = true;
            }
        }
        return flag;
    }

//BOOST akan dipakai ketika damage mobil = 0 dan jumlah weight di lane sekarang <= 0 atau hasil (banyaknya powerup BOOST)*(100) >= jumlah blok ke garis finish
    // Periksa ketersediaan BOOST
    public boolean IsBoostAvailable(){
        boolean flag = false;
        if (checkPowerUp(PowerUps.BOOST, myCar.powerups)){
            if (countPowerUp(PowerUps.BOOST, myCar.powerups)*100 >= 1500-myCar.position.block){
                flag = true;
            }
            else{
                if ((this.myCar.damage == 0) && laneRisk(myCar.position.lane, myCar.position.block, 15) <=0 ){
                    flag = true;
                }
            }

        }
        return flag;
    }

//LIZARD akan dipakai ketika jumlah 'weight' dari lane yang memungkinkan adalah sama dengan 0
    // Periksa ketersediaan LIZARD
    public boolean isLizardAvailable() {
        boolean flag = false;
        if (myCar.damage <=1 && myCar.speed == this.maxSpeed && laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) > 1 && checkPowerUp(PowerUps.LIZARD, myCar.powerups)){
            flag = true;
        }
        else{
            if (checkPowerUp(PowerUps.LIZARD, myCar.powerups)) {

                if (myCar.position.lane == 1) { /* Kalau mobil kita di lane paling atas atau lane pertama */
                    if (laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) > 0) { /* periksa jumlah weight di lane saat ini */
                        if (laneRisk((myCar.position.lane)+1, myCar.position.block, myCar.speed) > 0) { /* periksa jumlah weight di lane kanan */
                            flag = true;
                        }
                    }
                }

                else if (myCar.position.lane == 2 || myCar.position.lane == 3) { /* Kalau mobil kita di lane kedua atau ketiga */
                    if (laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) > 0) { /* periksa jumlah weight di lane saat ini */
                        if (laneRisk((myCar.position.lane)+1, myCar.position.block, myCar.speed) > 0) { /* periksa jumlah weight di lane kanan */
                            if (laneRisk((myCar.position.lane)-1, myCar.position.block, myCar.speed) > 0) { /* periksa jumlah weight di lane kiri */
                                flag = true;
                            }
                        }
                    }
                }
                else if (myCar.position.lane == 4) { /* Kalau mobil kita di lane terakhir atau lane keempat */
                    if (laneRisk(myCar.position.lane, myCar.position.block, myCar.speed) > 0) {  /* periksa jumlah weight di lane saat ini */
                        if (laneRisk((myCar.position.lane)-1, myCar.position.block, myCar.speed) > 0) {  /* periksa jumlah weight di lane kiri */
                            flag = true;
                        }
                    }
                }
            }
        }


        return flag;
    }

//TWEET akan diapaki tanpa syarat apapun
    // Periksa Ketersediaaan TWEET
    public boolean isTweetAvailable(){
        boolean flag = false;
        if (countPowerUp(PowerUps.TWEET, myCar.powerups) > 0){
            flag = true;
        }
        return flag;
    }

//OIL akan dipakai jika mobil kita ada di depan dan satu lane dengan mobil lawan
    // Periksa ketersediaan OIL
    public boolean isOilAvailable() {
        boolean flag = false;
        if (checkPowerUp(PowerUps.OIL, myCar.powerups)) {
            if(IsInFront(myCar.position.lane, opponent.position.lane, myCar.position.block, opponent.position.block)){
                if(IsInSameLane(myCar.position.lane, opponent.position.lane)) {
                    flag = true;
                }
            }
        }
        return flag;
    }
}