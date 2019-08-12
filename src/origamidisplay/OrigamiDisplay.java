package origamidisplay;

/**
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 */
public class OrigamiDisplay extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private int foldNumber;
    private boolean changeListenerShutUp;
    private int mouseX, mouseY;

    public OrigamiDisplay() {

        super();
        setTitle("Origami Viewer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        setLayout(new java.awt.BorderLayout());
        setMinimumSize(new java.awt.Dimension(300, 300));

        //display init
        final DisplayPanel dp = new DisplayPanel();
        dp.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {

                if (dp.PanelOrigami == null) {
                    return;
                }
                if (dp.PanelOrigami.circumscribedSquareSize() > 0) {
                    dp.PanelCamera.setZoom(0.8 * Math.min(dp.getWidth(), dp.getHeight()) / dp.PanelOrigami.circumscribedSquareSize());
                }
                dp.PanelCamera.xshift = dp.getWidth() / 2;
                dp.PanelCamera.yshift = dp.getHeight() / 2;
                dp.repaint();
            }
        });
        dp.addMouseListener(new java.awt.event.MouseAdapter() {
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {

                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });
        dp.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {

                dp.PanelCamera.rotate((mouseX - evt.getX()) / (float) dp.PanelCamera.zoom() / 2, (evt.getY() - mouseY) / (float) dp.PanelCamera.zoom() / 2);
                dp.repaint();
                mouseX = evt.getX();
                mouseY = evt.getY();
            }
        });
        dp.setBackground(java.awt.Color.white);
        dp.setPreferredSize(new java.awt.Dimension(300, 300));
        dp.setSize(new java.awt.Dimension(300, 300));
        add(dp, java.awt.BorderLayout.CENTER);

        //timeline init
        final javax.swing.JSlider timeSlider = new javax.swing.JSlider();
        timeSlider.setMinimum(0);
        timeSlider.setMaximum(0);
        timeSlider.setValue(0);
        foldNumber = 0;
        timeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                if (foldNumber == timeSlider.getValue() || changeListenerShutUp) {
                    return;
                }
                if (dp.PanelOrigami.history().size() < 100) {
                    if (foldNumber < timeSlider.getValue()) {
                        dp.PanelOrigami.redo(timeSlider.getValue() - foldNumber);
                    } else {
                        dp.PanelOrigami.undo(foldNumber - timeSlider.getValue());
                    }
                    foldNumber = timeSlider.getValue();
                } else { // Stop eating the CPU when it gets too complex
                    if (!timeSlider.getValueIsAdjusting()) {
                        if (foldNumber < timeSlider.getValue()) {
                            dp.PanelOrigami.redo(timeSlider.getValue() - foldNumber);
                        } else {
                            dp.PanelOrigami.undo(foldNumber - timeSlider.getValue());
                        }
                        foldNumber = timeSlider.getValue();
                    }
                }
                dp.PanelCamera.adjust(dp.PanelOrigami);
                dp.repaint();
            }
        });
        add(timeSlider, java.awt.BorderLayout.SOUTH);
        pack();

        try (java.io.InputStream fis = getClass().getResourceAsStream("/o")) {

            java.util.ArrayList<Byte> bytesb = new java.util.ArrayList<>();
            int fisbyte;
            while ((fisbyte = fis.read()) != -1) {
                bytesb.add((byte) fisbyte);
            }
            byte[] bytes = new byte[bytesb.size()];
            for (int i = 0; i < bytesb.size(); i++) {
                bytes[i] = bytesb.get(i);
            }

            int[] rgb = { 0, 0, 0x97 };
            dp.update(origamieditor3d.origami.OrigamiIO.read_gen2(new java.io.ByteArrayInputStream(bytes), rgb));
            dp.setFrontColor(rgb[0]*0x10000 + rgb[1]*0x100 + rgb[2]);
            if (dp.PanelOrigami.circumscribedSquareSize() > 0) {
                dp.PanelCamera.setZoom(0.8 * Math.min(dp.getWidth(), dp.getHeight()) / dp.PanelOrigami.circumscribedSquareSize());
            }
            dp.PanelCamera.adjust(dp.PanelOrigami);
            dp.PanelCamera.setOrthogonalView(0);
            dp.repaint();

            foldNumber = dp.PanelOrigami.history_pointer();
            changeListenerShutUp = true;
            timeSlider.setMaximum(dp.PanelOrigami.history().size());
            changeListenerShutUp = false;
            timeSlider.setValue(dp.PanelOrigami.history_pointer());

        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OrigamiDisplay().setVisible(true);
            }
        });
    }
}
