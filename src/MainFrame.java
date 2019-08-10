import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Random;

public class MainFrame {

    private JPanel pnlContainer;
    private JButton btnDraw;
    private JPanel pnlVoronoi;
    private JSpinner spnMaxVertici;
    private JLabel lblVertici;
    private boolean needRepaint = false;

    int maxVertex = 20;

    ColorModel colorModel = createColorModel();
    BufferedImage img;

    public MainFrame() {
        btnDraw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawVoronoi();
                pnlVoronoi.invalidate();
            }
        });

        pnlVoronoi.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                needRepaint=true;
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                needRepaint=true;
            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {
                needRepaint=true;
            }
        });

        spnMaxVertici.setValue(maxVertex);

        // Impedisco di inserire direttamente i valori
        ((JSpinner.DefaultEditor) spnMaxVertici.getEditor()).getTextField().setEditable(false);

        // Allungo la dimensione orizzontale a tre volte di quelle disegnate
        Dimension prefSize = ((JSpinner.DefaultEditor) spnMaxVertici.getEditor()).getTextField().getPreferredSize();
        ((JSpinner.DefaultEditor) spnMaxVertici.getEditor()).getTextField().setPreferredSize(new Dimension(prefSize.width*3,prefSize.height));

        spnMaxVertici.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                int val = (int) spinner.getValue();

                // Sotto a 5 vertici non si scende
                if (val<5){
                    val=5;
                    spinner.setValue(5);
                }
                maxVertex = val;
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        frame.setContentPane(new MainFrame().pnlContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Disegna il diagramma.
     * TODO: rendere parametrici i vertici nell'intervallo [0,1] per poi ridisegnare la mappa.
     * Indispensabile per ridisegnare la stessa mappa a risoluzioni e dimensioni diverse. DA FARE
     */
    private void drawVoronoi() {
        Graphics2D g = (Graphics2D) pnlVoronoi.getGraphics();
        int width = pnlVoronoi.getWidth()/2;
        int height = pnlVoronoi.getHeight()/2;
        int displacementX = width / 10;
        int displacementY = height / 10;

        //Color[][] mappa = new Color[width][height];

        Random rnd = new Random();

        int[] vertexX = new int[maxVertex];
        int[] vertexY = new int[maxVertex];

        byte[] mappa_1 = new byte[width*height];
        byte[] mappa_2 = new byte[width*height];
        byte[] mappa_3 = new byte[width*height];
        byte[] mappa_4 = new byte[width*height];

        // Vertici
        for (int i = 0; i < maxVertex; i++) {
            vertexX[i] = rnd.nextInt((int) (width * 0.8f)) + displacementX;
            vertexY[i] = rnd.nextInt((int) (height * 0.8f)) + displacementY;
// Vertici equipartiti
//            vertexX[i] = (i%5) * displacementX + displacementX;
//            vertexY[i] = (i/5) * displacementY + displacementY;
        }

        // Vriabili per i calcoli sui 4 subpanel
        double distanza;
        double [] distanze = new double[4];
        double distanzaOld;
        double[] distanzeOld = new double[4];
        //int vertexNearest=0;
        int[] verticiNearest = new int[4];

        // Ciclo sui pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

               // distanzaOld = 1e99;
                for (int i = 0; i < 4 ; i++) {
                    distanze[i]=0;
                    distanzeOld[i]=1e99;
                    verticiNearest[i]=0;
                }

                for (int vCounter = 0; vCounter < maxVertex; vCounter++) {

                    distanze[0]=(x - vertexX[vCounter]) * (x - vertexX[vCounter]) + (y - vertexY[vCounter]) * (y - vertexY[vCounter]);
                    distanze[1]=Math.sqrt((x - vertexX[vCounter]) * (x - vertexX[vCounter]) + (y - vertexY[vCounter]) * (y - vertexY[vCounter]));
                    distanze[2]=Math.abs(x - vertexX[vCounter]) + Math.abs(y - vertexY[vCounter]) ;
                    distanze[3]=Math.max(Math.abs(x - vertexX[vCounter]) , Math.abs(y - vertexY[vCounter])) ; // Chebyshev

                    for (int i = 0; i < 4; i++) {
                        if (distanze[i]<distanzeOld[i]){
                            distanzeOld[i]= distanze[i];
                            verticiNearest[i]=vCounter;
                        }
                    }
                }

                mappa_1[x+ y*width]=(byte)verticiNearest[0];
                mappa_2[x+ y*width]=(byte)verticiNearest[1];
                mappa_3[x+ y*width]=(byte)verticiNearest[2];
                mappa_4[x+ y*width]=(byte)verticiNearest[3];

            }

        }

        int stringPosx=10;
        int stringPosy=20;
        //g.setXORMode(Color.lightGray);
        g.setFont(new Font("Dialog",Font.BOLD,12));
        drawImage(g,mappa_1,width,height,0,0);
        g.drawString("No squared root", stringPosx,stringPosy);

        drawImage(g,mappa_2,width,height,width,0);
        g.drawString("Squared root", width+stringPosx,stringPosy);

        drawImage(g,mappa_3,width,height,0,height);
        g.drawString("Simple abs", stringPosx,height+ stringPosy);

        drawImage(g,mappa_4,width,height,width,height);
        g.drawString("Chebyshev", width + stringPosx, height + stringPosy);


        // Disegno i vertici (cerchio di raggio 4 pixel)
        for (int i = 0; i < maxVertex; i++) {
            g.setColor(Color.BLACK);
            g.fillOval(vertexX[i] - 2, vertexY[i] - 2, 4, 4);
            g.fillOval(width+ vertexX[i] - 2, vertexY[i] - 2, 4, 4);
            g.fillOval(vertexX[i] - 2, height + vertexY[i] - 2, 4, 4);
            g.fillOval(width+ vertexX[i] - 2, height+ vertexY[i] - 2, 4, 4);
            //g.drawLine(vertexX[i],vertexY[i],vertexX[i],vertexY[i]);
        }
    }


    /**
     * Creal il modello di colore per la mappa di byte
     * @return
     */
    private ColorModel createColorModel() {
        int colors = 256;
        byte[] reds = new byte[colors];
        byte[] greens = new byte[colors];
        byte[] blues = new byte[colors];

        Random rnd = new Random();
        byte[] rndBytes = new byte[3];
        // Genera i colori casuali
        for (int i = 0; i < colors; i++) {
            rnd.nextBytes(rndBytes);
            reds[i] = (byte) rndBytes[0] ;
            greens[i] = (byte) rndBytes[1];
            blues[i] = (byte) rndBytes[2];
//            reds[i] = (byte) i;
//            greens[i] = (byte) (i);
//            blues[i] = (byte) i;
        }
        return new IndexColorModel(8, colors, reds, greens, blues);
    }


    /**
     * Disegna il bulk di byte in un singolo colpo
     *
     * @param g
     * @param data
     * @param width
     * @param height
     * @param startx
     * @param starty
     */
    private void drawImage(Graphics g, byte[] data, int width, int height, int startx, int starty) {
        DataBuffer buffer = new DataBufferByte(data, data.length);

        SampleModel sm = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sm, buffer, null);

        // TODO: se non rigenero l'immagine non prende le nuove dimensioni in caso di resizing ?!?!?!
        if (img == null || needRepaint ) {
            img = new BufferedImage(colorModel, raster, false, null);
            needRepaint=false;
        } else {
            img.setData(raster);
        }

        g.drawImage(img, startx, starty, null);
    }


    /**
     * Genera i vertici. TODO: uso futuro evetuale. Meglio generare dei vertici nello spazio [0,1] per poi rimapparli in fase di disegnp - Da fare
     * @param maxVertexLocal
     * @param vx
     * @param vy
     * @param colors
     * @param width
     * @param height
     * @param displacementX
     * @param displacementY
     */
    private void generateVertex(int maxVertexLocal, int[] vx, int[] vy, Color[] colors, int width, int height, int displacementX, int displacementY){
        // colori dei vertici
        Random rnd = new Random();
        for (int i = 0; i < maxVertexLocal; i++) {
            colors[i] = new Color(
                    rnd.nextInt(255),
                    rnd.nextInt(255),
                    rnd.nextInt(255)
            );
        }

        // Vertici
        for (int i = 0; i < maxVertexLocal; i++) {
            vx[i] = rnd.nextInt((int) (width * 0.8f)) + displacementX;
            vy[i] = rnd.nextInt((int) (height * 0.8f)) + displacementY;
        }

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
