/*To do:
 - Add error codes for not inserting numbers in dialog box, or inserting crap instead of numbers
 - Add choice to make 32-bit image. Did not add it because contrast is not corrected with setMinandMax; needs an extra adjust contrast step.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.LutLoader;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.LUT;
import java.util.Random;
import static java.lang.Math.*;

public class Brownian_ implements PlugIn {

    @Override
    public void run(String f) {

        // Non-blocking dialog box for the user to input parameters
        String[] types = new String[]{"8-bit Black", "16-bit Black"};

        NonBlockingGenericDialog gd = new NonBlockingGenericDialog("Create a Brownian Universe");
        gd.addChoice("Image type:", types, "16-bit Black");
        gd.addNumericField("Universe width:", 200);
        gd.addNumericField("Universe height:", 200);
        gd.addNumericField("Number of frames:", 1000);
        gd.addNumericField("Number of particles:", 100);
        gd.addNumericField("Maximum step length in pixels:", 5);
        gd.addCheckbox("Colour particles?", true);
        gd.addCheckbox("Allow particles to cross Universe boundaries?", false);
        gd.showDialog();

        if (gd.wasCanceled()) return;

        int width = (int) gd.getNextNumber();
        int height = (int) gd.getNextNumber();
        int nSlices = (int) gd.getNextNumber();
        int nParticles= (int) gd.getNextNumber();
        int max_radius = (int) gd.getNextNumber();
        boolean colour = gd.getNextBoolean();
        boolean cross = gd.getNextBoolean();

        // Create image stack
        ImagePlus universe = IJ.createImage("Brownian Universe", gd.getNextChoice(), width, height, nSlices);
        ImageProcessor ip = universe.getProcessor();
        ip.setMinAndMax(0, nParticles);

        if (colour==true) {
            LUT lut = LutLoader.openLut("/Applications/Fiji.app/luts/16_colors.lut");
            ip.setLut(lut);
        }

        // Initial position of each particle
        Random rand = new Random();

        float[] x0 = new float[nParticles];
        float[] y0 = new float[nParticles];

        for (int i=0; i<nParticles; i++) {
            x0[i]=rand.nextFloat()*(width-1);
            y0[i]=rand.nextFloat()*(height-1);

        }

        // Random motion
        // Loop to iterate across all slices of the stack
        for (int s=1; s<=nSlices; s++) {
            universe.setSlice(s);

            // Nested loop to generate X and Y arrays containing the coordinates of the particles in each slice
            for (int n=0; n<nParticles; n++) {
                float A = rand.nextFloat()*max_radius; // Step amplitude
                float alpha = (float) (rand.nextFloat()*2*PI); // Step angle (in radians)

                int x1 = (int) round(A*cos(alpha)+x0[n]); // Random motion in X
                int y1 = (int) round(A*sin(alpha)+y0[n]); // Random motion in Y


                // Conditions for particles contained in the box (comment-out to allow them to cross the boundaries)
                if (cross==false) {
                    if (x1 < 0) x1 = abs(x1);
                    if (y1 < 0) y1 = abs(y1);
                    if (x1 > (width - 1)) x1 = (width - 1) - (x1 - (width - 1));
                    if (y1 > (height - 1)) y1 = (height - 1) - (y1 - (height - 1));
                }
                // Condition for particles (allows crossing the boundaries of the box)
                if (cross==true) {
                   if (x1 >= 0 && y1 >= 0 && x1 < width && y1 < height);
                }

                ip.putPixel(x1,y1,n);

                x0[n]=x1;
                y0[n]=y1;
            }
        }
        universe.show();
    }
}
