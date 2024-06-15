# 3D Jumper

When running the code, do not move your mouse until you see the screen appear. The mouse is calibrating during this time, and the camera will spin uncontrollably if your mouse moves. If this happens, hold your mouse completely still and press c to recalibrate.

The goal of this game is to get to the top of the world! Climb up by jumping across cubes to get from the floor to the ceiling!

Mechanics:
- Movement: Use W,A,S and D to move around, use SPACE to jump. Use the mouse to look around.
- Pause: Press ESC to pause. Pause menu includes resetting the mouse, exiting the program, and turning devstats on.
- Collisions: Not only can you move, but you can also collide with things! The player will stop at any defined hitbox.

If keyboard controls don't work, try clicking once with your mouse. When the program launches, your computer's focus is not on that window, so keyboard inputs won't be read until you click.

Limitations:
- Hitboxes: Hitboxes are only based on non-rotatable rectangular prisims. This means that triangular hitboxes are not availible. However, more hitboxes can be added using the addHitbox function, and these squares can be arranged to form more complex objects, so there is a way around this.
- Mouse: After reseting the mouse from the menu, moving the mouse at all before unpausing will cause the same spinning effect as when the game first launches.
- FPS: This game is unoptimized, meaning that you'll be getting anywhere from 5-60FPS while playing, averaging ~20 fps.

