package za.co.entelect.challenge;

import jdk.internal.net.http.websocket.WebSocketImpl;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;


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

        directionList.add(-1);
        directionList.add(1);
    }

    // Strategi Utama
    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        if (this.myCar.damage >= 5) {
            return FIX;
        }
        
        
        /** kalo lanenya sama dan mobil kita ada di depan, bisa buang oli **/
        if(IsInSameLane(myCar.position.lane, opponent.position.lane)){
            if(IsInFront(myCar.position.lane, opponent.position.lane, opponent.position.block, opponent.position.block)){
                if (checkPowerUp(PowerUps.OIL, myCar.powerups)) {
                    return OIL;
                }
            }
        }
       
        
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        if (IsBoostAvailable()){
            return BOOST;
        }

        else if (blocks.contains(Terrain.MUD)) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }
        return new AccelerateCommand();
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
    
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
    
    private boolean checkPowerUp(PowerUps check_power, PowerUps[] existing){
        for(PowerUps power: existing){
            if(power.equals(check_power)){
                return true;
            }
        }
        return false;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Menghitung bobot lane dalam param speed block ke depan
    private int laneRisk(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        int weight = 0;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            else if (laneList[i].terrain == Terrain.MUD || laneList[i].terrain == Terrain.OIL_SPILL){
                weight += 1;
            }
            else if (laneList[i].terrain == Terrain.WALL){
                weight += 3;
            }
            else if (laneList[i].terrain == Terrain.BOOST || laneList[i].terrain == Terrain.LIZARD){
                weight -= 3;
            }
            else if (laneList[i].terrain == Terrain.EMP || laneList[i].terrain == Terrain.OIL_POWER || laneList[i].terrain == Terrain.TWEET){
                weight -= 2;
            }
            blocks.add(laneList[i].terrain);

        }
        return weight;
    }

    // Periksa ketersediaan BOOST
    public boolean IsBoostAvailable(){
        boolean flag = false;
        if ((this.myCar.damage == 0) && laneRisk(myCar.position.lane, myCar.position.block, 15) <=0 && hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            flag = true;
        }
        return flag;
    }

}
