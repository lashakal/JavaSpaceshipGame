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
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 60;
    private static final int OBSTACLE_WIDTH = 20;
    private static final int OBSTACLE_HEIGHT = 20;
    private static final int HEALTH_POWER_UP_WIDTH = 20;
    private static final int HEALTH_POWER_UP_HEIGHT = 20;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 10;
    private static final int OBSTACLE_SPEED = 3;
    private static final int HEALTH_POWER_UP_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private int spriteWidth = 20;
    private int spriteHeight = 20;
    private int score = 0;
    private int health = 100;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private BufferedImage shipImage;
    private BufferedImage spriteSheet;
    private BufferedImage healthPowerUpImage;
    private Clip fireClip;
    private Clip collisionClip;
    private boolean shieldActive = false;
    private int shieldDuration = 5000;  // in milliseconds
    private long shieldStartTime;

    private List<Point> obstacles;
    private List<Point> healthPowerUps;
    private List<Point> stars;

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

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
    }

    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
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

            scoreLabel.setText("Score: " + score);
            healthLabel.setText("Health: " + health);
        }
    }

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

    public static Color generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return new Color(r, g, b);
    }

    public void playFiringSound() {
        if (fireClip != null) {
            fireClip.setFramePosition(0);
            fireClip.start();
        }
    }

    public void playCollisionSound() {
        if (collisionClip != null) {
            collisionClip.setFramePosition(0);
            collisionClip.start();
        }
    }

    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }

    private void deactivateShield() {
        shieldActive = false;
    }

    private boolean isShieldActive() {
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }

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

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}
