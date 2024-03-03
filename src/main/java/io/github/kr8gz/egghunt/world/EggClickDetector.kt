package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Egg
import io.github.kr8gz.egghunt.eggHuntMessage
import io.github.kr8gz.egghunt.foundEgg
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.item.FireworkRocketItem
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EggClickDetector {
    fun registerBlockClickListeners() {
        UseBlockCallback.EVENT.register { player, world, hand, result ->
            if (hand == Hand.MAIN_HAND) {
                eggFindListener(player, world, result.blockPos)
            }
            ActionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, _, pos, _ ->
            eggFindListener(player, world, pos)
            ActionResult.PASS
        }
    }

    private fun eggFindListener(player: PlayerEntity, world: World, pos: BlockPos) {
        Egg.findAtLocation(pos)?.run {
            if (player.foundEgg(this)) {
                player.eggHuntMessage("You found an egg!", Formatting.GREEN)
                spawnFirework(world, pos)
            } else {
                player.eggHuntMessage("You already found this egg!", Formatting.RED)
            }
        }
    }

    private fun spawnFirework(world: World, pos: BlockPos) {
        val fireworkItem = Items.FIREWORK_ROCKET.defaultStack.apply {
            getOrCreateSubNbt(FireworkRocketItem.FIREWORKS_KEY).apply {
                putByte(FireworkRocketItem.FLIGHT_KEY, 0)
                put(FireworkRocketItem.EXPLOSIONS_KEY, NbtList().apply {
                    add(NbtCompound().apply {
                        putByte(FireworkRocketItem.TYPE_KEY, FireworkRocketItem.Type.BURST.id.toByte())
                        putIntArray(FireworkRocketItem.COLORS_KEY, intArrayOf(DyeColor.entries.random().fireworkColor))
                    })
                })
            }
        }

        world.spawnEntity(with(pos.toCenterPos()) {
            FireworkRocketEntity(world, x, y, z, fireworkItem)
        })
    }
}
