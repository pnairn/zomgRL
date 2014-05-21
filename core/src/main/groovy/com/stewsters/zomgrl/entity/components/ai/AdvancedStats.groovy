package com.stewsters.zomgrl.entity.components.ai

import com.stewsters.util.math.MatUtils
import com.stewsters.zomgrl.entity.Entity
import com.stewsters.zomgrl.entity.components.item.Equipment
import com.stewsters.zomgrl.entity.components.item.Item
import com.stewsters.zomgrl.entity.components.item.Slot

public class AdvancedStats extends BaseAi implements Ai {

    private float morale
    private float chargeProbability
    private float retreatProbability


    public AdvancedStats(Map params) {
        morale = params?.morale ?: 0.5f
        chargeProbability = params?.chargeProbability ?: 0.5f
        retreatProbability = params?.retreatProbability ?: 0.5f

    }

    // http://dillingers.com/blog/2014/05/10/roguelike-ai/
    public void takeTurn() {

        //nearest opponent
        Entity enemy = findClosestVisibleEnemy()
        int optimalRange = 1


        if (enemy) {
            int enemyDistance = owner.distanceTo(enemy)

            Equipment weapon
            Item item

            int minRange = 1
            int maxRange = 1

            if (owner.inventory) {
                weapon = owner.inventory.getEquippedInSlot(Slot.rightHand)
                if (weapon) {
                    item = weapon.owner.itemComponent
                    if (item) {
                        minRange = item.minRange
                        maxRange = item.maxRange
                        optimalRange = (minRange + maxRange) / 2
                    }
                }
            }

            //if we have a gun, and they are getting too close, shoot them
            if (owner.fighter && (((float) owner.fighter.hp / owner.fighter.maxHP) < morale)) {
//                //flee
                if (!owner.moveAway(enemy.x, enemy.y)) {
                    owner.moveTowardsAndAttack(enemy.x, enemy.y)
                }
            } else if (enemyDistance > optimalRange && canAttack(enemyDistance, minRange, maxRange) && canMoveToward(enemy)) {

                if (MatUtils.getFloatInRange(0, 1) < chargeProbability) {
                    owner.moveTowardsAndAttack(enemy.x, enemy.y)
                } else {
                    attackTarget(enemy, item)
                }
            } else if (enemyDistance < optimalRange && canAttack(enemyDistance, minRange, maxRange) && canMoveAway(enemy)) {
                // to close
                if (MatUtils.getFloatInRange(0, 1) < retreatProbability) {
                    owner.moveAway(enemy.x, enemy.y)
                } else {
                    attackTarget(enemy, item)
                }
            } else if (canAttack(enemyDistance, minRange, maxRange)) {
                attackTarget(enemy, item)

            } else if (enemyDistance > optimalRange && canMoveToward(enemy)) {
                owner.moveTowardsAndAttack(enemy.x, enemy.y)

            } else if (enemyDistance < optimalRange && canMoveAway(enemy)) {
                owner.moveAway(enemy.x, enemy.y)

            } else if (MatUtils.boolean) {
                owner.randomMovement();
            }


        } else if (owner.inventory) {

            //if we are standing on an item and we have room, pick it up
            if (owner.inventory.isFull()) {
                owner.randomMovement()
            } else {
                //find nearest visible item
                Entity item = owner.ai.findClosestVisibleItem()

                //if we are standing on it, pickUp
                if (item) {
                    if (item.x == owner.x && item.y == owner.y) {
                        owner.inventory.pickUp(item)
                    } else {
                        owner.moveTowardsAndAttack(item.x, item.y)
                    }
                } else {
                    owner.randomMovement()
                }

            }
        } else if (MatUtils.boolean) {
            owner.randomMovement();
        }

    }

    boolean canMoveAway(Entity target) {
        int dx = target.x - owner.x
        int dy = target.y - owner.y

        dx = MatUtils.limit(dx, -1, 1)
        dy = MatUtils.limit(dy, -1, 1)

        return owner.levelMap.isBlocked(owner.x + dx, owner.y + dy)
    }


    boolean canMoveToward(Entity target) {
        int dx = target.x - owner.x
        int dy = target.y - owner.y
        float distance = Math.sqrt(dx**2 + dy**2)
        dx = (int) Math.round(dx / distance)
        dy = (int) Math.round(dy / distance)

        return owner.levelMap.isBlocked(owner.x + dx, owner.y + dy)
    }

    boolean canAttack(int targetRange, int minRange, int maxRange) {
        //todo: los?  we select based on view, so it may not matter
        return minRange >= targetRange && maxRange <= targetRange
    }

    void attackTarget(Entity entity, Item item) {
        if (item && item.useFunction != null) {
            item.useHeldItem(owner)
            println "Bang!"
        } else {
            owner.moveTowardsAndAttack(entity.x, entity.y)
        }
    }

}
