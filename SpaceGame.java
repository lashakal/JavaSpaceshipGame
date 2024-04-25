/**

 * Project: Solo Lab 7 Assignment
 * Purpose Details: Space Game
 * Course: IST 242
 * Author: Lasha Kaliashvili
 * Date Developed: 04/24/2024
 * Last Date Changed: 04/24/2024
 * Rev: 1

 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceGame extends JFrame implements KeyListener {
    /**
     * Width of the game window
     */
    private static final int WIDTH = 500;
    /**
     * Height of the game window
     */
    private static final int HEIGHT = 500;
    /**
     * Player's width
     */
    private static final int PLAYER_WIDTH = 50;
    /**
     * Player's height
     */
    private static final int PLAYER_HEIGHT = 60;
    /**
     * Obstacle's width
     */
    private static final int OBSTACLE_WIDTH = 20;
    /**
     * Obstacle's height
     */
    private static final int OBSTACLE_HEIGHT = 20;
    /**
     * Width of a health power-up
     */
    private static final int HEALTH_POWER_UP_WIDTH = 20;
    /**
     * Height of a health power-up
     */
    private static final int HEALTH_POWER_UP_HEIGHT = 20;
    /**
     * Projectile's width
     */
    private static final int PROJECTILE_WIDTH = 5;
    /**
     * Projectile's height
     */
    private static final int PROJECTILE_HEIGHT = 10;
    /**
     * Player's speed
     */
    private static final int PLAYER_SPEED = 10;
    /**
     * Obstacle speed in regular mode (score < 100)
     */
    private static final int OBSTACLE_SPEED_REGULAR = 3;
    /**
     * Obstacle speed in harder mode (score >= 100)
     */
    private static final int OBSTACLE_SPEED_HARD = 5;
    /**
     * Health power-up's moving speed
     */
    private static final int HEALTH_POWER_UP_SPEED = 3;
    /**
     * Projectile's moving speed
     */
    private static final int PROJECTILE_SPEED = 10;
    /**
     * Width of the obstacle sprite
     */
    private int spriteWidth = 20;
    /**
     * Width of the obstacle sprite
     */
    private int spriteHeight = 20;
    /**
     * Game score
     */
    private int score = 0;
    /**
     * Player's health
     */
    private int health = 100;

    /**
     * Game window
     */
    private JPanel gamePanel;
    /**
     * Text label to display score
     */
    private JLabel scoreLabel;
    /**
     * Text label to display health
     */
    private JLabel healthLabel;
    /**
     * Text label to display countdown timer
     */
    private JLabel timerLabel;
    /**
     * Timer
     */
    private Timer timer;
    /**
     * Shows whether the game is over or not
     */
    private boolean isGameOver;
    /**
     * Player's position
     */
    private int playerX, playerY;
    /**
     * Projectile's position
     */
    private int projectileX, projectileY;
    /**
     * Shows whether projectile is visible or not
     */
    private boolean isProjectileVisible;
    /**
     * Shows whether the player is firing or not
     */
    private boolean isFiring;
    /**
     * Image of the spaceship
     */
    private BufferedImage shipImage;
    /**
     * Sprite sheet of obstacles
     */
    private BufferedImage spriteSheet;
    /**
     * Image of the health power-up
     */
    private BufferedImage healthPowerUpImage;
    /**
     * Firing sound clip
     */
    private Clip fireClip;
    /**
     * Collision sound clip
     */
    private Clip collisionClip;
    /**
     * Shows whether the shield is active or not
     */
    private boolean shieldActive = false;
    /**
     * Duration of the game = 60 seconds
     */
    private int gameDuration = 60000;
    /**
     * Start time of the game
     */
    private long gameStartTime;
    /**
     * Shield duration = 5 seconds
     */
    private int shieldDuration = 5000;  // in milliseconds
    /**
     * When shield was activated
     */
    private long shieldStartTime;
    /**
     * Shows whether we are in the harder level or not
     */
    private boolean hardLevel = false;

    /**
     * List to store obstacles
     */
    private List<Point> obstacles;
    /**
     * List to store health power-ups
     */
    private List<Point> healthPowerUps;
    /**
     * List to store points representing stars
     */
    private List<Point> stars;

    /**
     * Constructor method to set up the game
     */
    public SpaceGame() {
        try {
            shipImage = ImageIO.read(new File("spaceship.png"));
            spriteSheet = ImageIO.read(new File("obstacleSprite.png"));
            healthPowerUpImage = ImageIO.read(new File("health.png"));

            AudioInputStream fireAudioInputStream = AudioSystem.getAudioInputStream(new File("fire.wav").getAbsoluteFile());
            fireClip = AudioSystem.getClip();
            fireClip.open(fireAudioInputStream);

            AudioInputStream collisionAudioInputStream = AudioSystem.getAudioInputStream(new File("collision.wav").getAbsoluteFile());
            collisionClip = AudioSystem.getClip();
            collisionClip.open(collisionAudioInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        }

        stars = generateStars(200);

        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 100");
        healthLabel.setBounds(10, 10, 100, 20);
        healthLabel.setForeground(Color.WHITE);
        gamePanel.add(healthLabel);

        timerLabel = new JLabel("Time: 60s");
        timerLabel.setBounds(10, 10, 100, 20);
        timerLabel.setForeground(Color.WHITE);
        gamePanel.add(timerLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();
        healthPowerUps = new java.util.ArrayList<>();

        gameStartTime = System.currentTimeMillis();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
    }

    /**
     * Update the graphics of the game by redrawing changing components
     *
     * @param g Graphics of the game
     */
    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // g.setColor(Color.BLUE);
        // g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        g.drawImage(shipImage, playerX, playerY, null);

        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        if (isShieldActive()) {
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(playerX, playerY, 50, 60);
        }

        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                // Randomly select a sprite index 0-3
                Random random = new Random();
                int spriteIndex = random.nextInt(4);

                // Calculate x and y coordinates of the selected sprite on the sprite sheet
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0;

                // Draw the selected sprite onto the canvas
                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY, spriteWidth, spriteHeight), obstacle.x, obstacle.y, null);
            }
        }

        // display image for health power-ups
        for (Point healthPowerUp : healthPowerUps) {
            g.drawImage(healthPowerUpImage, healthPowerUp.x, healthPowerUp.y, null);
        }

        g.setColor(generateRandomColor());
        for (Point star : stars) {
            g.fillOval(star.x, star.y, 2, 2);
        }

        // Score label becomes red in the harder level
        if (hardLevel) {
            scoreLabel.setForeground(Color.RED);
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    /**
     * Update the fields of the SpaceGame
     */
    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                if (!hardLevel) {
                    obstacles.get(i).y += OBSTACLE_SPEED_REGULAR;
                } else {
                    obstacles.get(i).y += OBSTACLE_SPEED_HARD;  // obstacles move faster in the harder level
                }
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles
            if (!hardLevel) {
                if (Math.random() < 0.02) {
                    int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                    obstacles.add(new Point(obstacleX, 0));
                }
            } else {
                if (Math.random() < 0.05) {     // generate more obstacles if in the harder level
                    int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                    obstacles.add(new Point(obstacleX, 0));
                }
            }

            // Move health points
            for (int i = 0; i < healthPowerUps.size(); i++) {
                healthPowerUps.get(i).y += HEALTH_POWER_UP_SPEED;
                if (healthPowerUps.get(i).y > HEIGHT) {
                    healthPowerUps.remove(i);
                    i--;
                }
            }

            // Generate new health points
            if (Math.random() < 0.001) {
                int healthPowerUpX = (int) (Math.random() * (WIDTH - HEALTH_POWER_UP_WIDTH));
                healthPowerUps.add(new Point(healthPowerUpX, 0));
            }

            if (Math.random() < 0.1) {
                stars = generateStars(200);
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !isShieldActive()) {
                    playCollisionSound();
                    obstacles.remove(i);
                    health -= 20;
                    if (health <= 0) {
                        isGameOver = true;
                        break;
                    }
                }
            }

            // Check collision with obstacle
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    isProjectileVisible = false;
                    break;
                }
            }

            for (int i = 0; i < healthPowerUps.size(); i++) {
                Rectangle healthRect = new Rectangle(healthPowerUps.get(i).x, healthPowerUps.get(i).y, HEALTH_POWER_UP_WIDTH, HEALTH_POWER_UP_HEIGHT);
                if (projectileRect.intersects(healthRect)) {
                    healthPowerUps.remove(i);
                    health += 20;
                    isProjectileVisible = false;
                    break;
                }
            }

            int timeLeft = (int) ((gameDuration - (System.currentTimeMillis() - gameStartTime)) / 1000);

            // If score reaches 100, switch to the harder level
            if (score >= 100) {
                hardLevel = true;
            }

            scoreLabel.setText("Score: " + score);
            healthLabel.setText("Health: " + health);
            timerLabel.setText("Time Left: " + timeLeft + "s");

            if (isGameTimeUp()) {
                isGameOver = true;
            }
        }
    }

    /**
     * Generate stars
     *
     * @param numStars  number of stars
     * @return          List of generated stars
     */
    private List<Point> generateStars(int numStars) {
        List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numStars; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x, y));
        }
        return starsList;
    }

    /**
     * Generate a random color
     *
     * @return  random color that was generated
     */
    public static Color generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }

    /**
     * Play firing sound
     */
    public void playFiringSound() {
        if (fireClip != null) {
            fireClip.setFramePosition(0);
            fireClip.start();
        }
    }

    /**
     * Play collision sound
     */
    public void playCollisionSound() {
        if (collisionClip != null) {
            collisionClip.setFramePosition(0);
            collisionClip.start();
        }
    }

    /**
     * Activate the shield
     */
    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }

    /**
     * Deactivate the shield
     */
    private void deactivateShield() {
        shieldActive = false;
    }

    /**
     * Check if the shield is active
     *
     * @return  True if shield is active and it's time is not up; otherwise, false
     */
    private boolean isShieldActive() {
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }

    /**
     * Check if  time is up for the whole game
     *
     * @return  True if time is up
     */
    private boolean isGameTimeUp() {
        return (System.currentTimeMillis() - gameStartTime) > gameDuration;
    }

    /**
     * Process key presses
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            playFiringSound();
            isFiring = true;
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // Limit firing rate
                        isFiring = false;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (keyCode == KeyEvent.VK_CONTROL) {
            activateShield();
        }
    }

    /**
     * Process typed keys
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Process key release
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * Main method to run the space game
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}