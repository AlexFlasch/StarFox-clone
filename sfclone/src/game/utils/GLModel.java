package game.utils;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.sun.prism.impl.BufferUtil;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author wjur on Github
 *
 * Modified by Alex Flasch to use JOGL
 */

public class GLModel{

    private ArrayList<double[]> vertSets;
    private ArrayList<double[]> vertSetNorms;
    private ArrayList<double[]> vertSetTextures;
    private ArrayList<int[]> faces;
    private ArrayList<int[]> faceTextures;
    private ArrayList<int[]> faceNorms;
    private ArrayList<String[]> mtlTimings;
    private ArrayList<Texture> glTextures;
    private int[] boundTextures;
    private MtlLoader materials;
    private ArrayList<Integer> objectLists;
    private int numPolys;
    public double topPoint;
    public double bottomPoint;
    public double leftPoint;
    public double rightPoint;
    public double farPoint;
    public double nearPoint;
    private String mtlPath;
    private int numTextures;

    private GLU glu;

    //THIS CLASS LOADS THE MODELS
    GLModel(BufferedReader ref, boolean centerIt, String path, GL2 gl){
//        gl.glEnable(GL2.GL_BLEND);
//        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        GLCapabilities capabilities = new GLCapabilities(gl.getGLProfile());
        capabilities.setRedBits(32);
        capabilities.setGreenBits(32);
        capabilities.setBlueBits(32);
        capabilities.setAlphaBits(32);

        glu = new GLU();

        mtlPath = path;
        vertSets = new ArrayList<>();
        vertSetNorms = new ArrayList<>();
        vertSetTextures = new ArrayList<>();
        faces = new ArrayList<>();
        faceTextures = new ArrayList<>();
        faceNorms = new ArrayList<>();
        glTextures = new ArrayList<>();
        mtlTimings = new ArrayList<>();
        objectLists = new ArrayList<>();
        numTextures = 0;
        numPolys = 0;
        topPoint = 0.0F;
        bottomPoint = 0.0F;
        leftPoint = 0.0F;
        rightPoint = 0.0F;
        farPoint = 0.0F;
        nearPoint = 0.0F;
        loadObject(ref);
        if(centerIt)
            centerIt();
        try{
            drawToList(gl);
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found.");
            e.getMessage();
        }
        numPolys = faces.size();
        cleanup();
    }

    private void cleanup(){
        vertSets.clear();
        vertSetNorms.clear();
        vertSetTextures.clear();
        faces.clear();
        faceTextures.clear();
        faceNorms.clear();
    }

    private void loadObject(BufferedReader br){
        int faceCounter = 0;
        try{
            boolean firstPass = true;
            String newline;
            while((newline = br.readLine()) != null){
                if(newline.length() > 0){
                    newline = newline.trim();

                    //LOADS VERTEX COORDINATES
                    if(newline.startsWith("v ")){
                        double coords[] = new double[4];
                        newline = newline.substring(2, newline.length());
                        StringTokenizer st = new StringTokenizer(newline, " ");
                        for(int i = 0; st.hasMoreTokens(); i++)
                            coords[i] = Double.parseDouble(st.nextToken());

                        if(firstPass){
                            rightPoint = coords[0];
                            leftPoint = coords[0];
                            topPoint = coords[1];
                            bottomPoint = coords[1];
                            nearPoint = coords[2];
                            farPoint = coords[2];
                            firstPass = false;
                        }
                        if(coords[0] > rightPoint)
                            rightPoint = coords[0];
                        if(coords[0] < leftPoint)
                            leftPoint = coords[0];
                        if(coords[1] > topPoint)
                            topPoint = coords[1];
                        if(coords[1] < bottomPoint)
                            bottomPoint = coords[1];
                        if(coords[2] > nearPoint)
                            nearPoint = coords[2];
                        if(coords[2] < farPoint)
                            farPoint = coords[2];
                        vertSets.add(coords);
                    }
                    else {

                        //LOADS VERTEX TEXTURE COORDINATES
                        if (newline.startsWith("vt")) {
                            double coords[] = new double[4];
                            newline = newline.substring(3, newline.length());
                            StringTokenizer st = new StringTokenizer(newline, " ");
                            for (int i = 0; st.hasMoreTokens(); i++)
                                coords[i] = Double.parseDouble(st.nextToken());

                            vertSetTextures.add(coords);
                        } else {

                            //LOADS VERTEX NORMALS COORDINATES
                            if (newline.startsWith("vn")) {
                                double coords[] = new double[4];
                                newline = newline.substring(3, newline.length());
                                StringTokenizer st = new StringTokenizer(newline, " ");
                                for (int i = 0; st.hasMoreTokens(); i++)
                                    coords[i] = Double.parseDouble(st.nextToken());

                                vertSetNorms.add(coords);
                            } else {

                                //LOADS FACES COORDINATES
                                if (newline.startsWith("f ")) {
                                    faceCounter++;
                                    newline = newline.substring(2, newline.length());
                                    StringTokenizer st = new StringTokenizer(newline, " ");
                                    int count = st.countTokens();
                                    int v[] = new int[count];
                                    int vt[] = new int[count];
                                    int vn[] = new int[count];
                                    for (int i = 0; i < count; i++) {
                                        char chars[] = st.nextToken().toCharArray();
                                        StringBuilder sb = new StringBuilder();
                                        char lc = 'x';
                                        for (char c : chars) {
                                            if (c == '/' && lc == '/')
                                                sb.append('0');
                                            lc = c;
                                            sb.append(lc);
                                        }

                                        StringTokenizer st2 = new StringTokenizer
                                                (sb.toString(), "/");
                                        int num = st2.countTokens();
                                        v[i] = Integer.parseInt(st2.nextToken());
                                        if (num > 1)
                                            vt[i] = Integer.parseInt(st2.nextToken());
                                        else
                                            vt[i] = 0;
                                        if (num > 2)
                                            vn[i] = Integer.parseInt(st2.nextToken());
                                        else
                                            vn[i] = 0;
                                    }

                                    faces.add(v);
                                    faceTextures.add(vt);
                                    faceNorms.add(vn);
                                } else {

                                    //LOADS MATERIALS
                                    if (newline.startsWith("mtllib")) {
                                        if (mtlPath != null)
                                            loadMaterials();
                                    } else {

                                        //USES MATERIALS
                                        if (newline.startsWith("usemtl")) {
                                            numTextures++;
                                            String[] coords = new String[2];
                                            String[] textureCoords;
                                            textureCoords = newline.split("\\s+");
                                            coords[0] = textureCoords[1];
                                            coords[1] = faceCounter + "";
                                            mtlTimings.add(coords);
                                            // System.out.println(coords[0] + ", " + coords[1]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(IOException e){
            System.out.println("Failed to read file: " + br.toString());
        }
        catch(NumberFormatException e){
            System.out.println("Malformed OBJ file: " + br.toString() + "\r \r"+ e.getMessage());
        }
    }

    private void generateTexture(GL2 gl, int i) {
        if(materials.hasTextureMap) {
            int kaTexture;
            int kdTexture;
            int ksTexture;

            String[] temp;
            temp = Arrays.copyOf(mtlPath.split("/"), mtlPath.split("/").length - 1);
            String resPath;
            StringBuilder sb = new StringBuilder();

            for (String s : temp) {
                sb.append(s).append("/");
            }

            resPath = sb.toString();
            String mtlName = mtlTimings.get(i)[0];
            boundTextures = new int[mtlTimings.size()];

//            gl.glGenTextures(mtlTimings.size(), boundTextures, 0);

            try {
                URL textureUrl = new URL("file", "localhost", (resPath + mtlName + ".png"));

                if (materials.kaTexturePath != null) {
//                    gl.glBindTexture(GL.GL_TEXTURE_2D, kaTexture);
                    BufferedImage img = readPng(resPath + mtlName + ".png");
                    File imgFile = new File(resPath + mtlName + ".png");
                    assert img != null;
                    if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                        glTextures.add(TextureIO.newTexture(textureUrl, true, null));
                    } else {
                        makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                    }
                }

                if (materials.kdTexturePath != null) {
//                    gl.glBindTexture(GL.GL_TEXTURE_2D, kdTexture);
                    BufferedImage img = readPng(resPath + mtlName+ ".png");
                    File imgFile = new File(resPath + mtlName + ".png");
                    assert img != null;
                    if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                        TextureData data = TextureIO.newTextureData(gl.getGLProfile(), textureUrl, true, TextureIO.PNG);
                        glTextures.add(TextureIO.newTexture(new File(resPath + mtlName + ".png"), true));
                    } else {
                        makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                    }
                }

                if (materials.ksTexturePath != null) {
//                    gl.glBindTexture(GL.GL_TEXTURE_2D, ksTexture);
                    BufferedImage img = readPng(resPath + mtlName + ".png");
                    assert img != null;
                    if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                        glTextures.add(TextureIO.newTexture(textureUrl, true, null));
                    } else {
                        makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                    }
                }

                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_MIRRORED_REPEAT);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_MIRRORED_REPEAT);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void loadMaterials() {
        FileReader frm;
        String mtlRef = mtlPath;

        try {
            frm = new FileReader(mtlRef);
            BufferedReader brm = new BufferedReader(frm);
            materials = new MtlLoader(brm, mtlPath);
            frm.close();
        } catch (IOException e) {
            System.out.println("Could not open file: " + mtlRef);
            materials = null;
        }
    }

    private void centerIt(){
        double xShift = (rightPoint - leftPoint) / 2.0F;
        double yShift = (topPoint - bottomPoint) / 2.0F;
        double zShift = (nearPoint - farPoint) / 2.0F;
        for(int i = 0; i < vertSets.size(); i++){
            double coords[] = new double[4];
            coords[0] = (vertSets.get(i))[0] - leftPoint - xShift;
            coords[1] = (vertSets.get(i))[1] - bottomPoint - yShift;
            coords[2] = (vertSets.get(i))[2] - farPoint - zShift;
            vertSets.set(i, coords);
        }

    }

    public double getXWidth(){
        return rightPoint - leftPoint;
    }

    public double getYHeight(){
        return topPoint - bottomPoint;
    }

    public double getZDepth(){
        return nearPoint - farPoint;
    }

    public int numPolygons(){
        return numPolys;
    }

    private void drawToList(GL2 gl) throws FileNotFoundException {
        ////////////////////////////////////////
        /// With Materials if available ////////
        ////////////////////////////////////////

        int nextMtl = -1;
        int mtlCount = 0;
        int totalMtls = mtlTimings.size();
        String[] nextMtlNameArr;
        String nextMtlName = null;

        int stopAtFace = 0;
        int previousStopAtFace = 0;

        for(int i = 0; i < numTextures; i++) {
            this.objectLists.add(gl.glGenLists(1));

            if(i == mtlTimings.size() - 1) {
                previousStopAtFace = stopAtFace;
                stopAtFace = faces.size();
            }
            else {
                previousStopAtFace = stopAtFace;
                stopAtFace = Integer.parseInt(mtlTimings.get(i + 1)[1]);
            }

            if (totalMtls > 0 && materials != null) {
                nextMtlNameArr = mtlTimings.get(mtlCount);
                nextMtlName = nextMtlNameArr[0];
                nextMtl = Integer.parseInt(nextMtlNameArr[1]);
            }

            gl.glNewList(objectLists.get(i), GL2.GL_COMPILE);
            for (int j = previousStopAtFace; j < stopAtFace; j++) {
                if (j == nextMtl) {
                    gl.glEnable(GL2.GL_TEXTURE_2D);
                    generateTexture(gl, i);
                    gl.glEnable(GL2.GL_COLOR_MATERIAL);
                    gl.glColor4f((materials.getKd(nextMtlName))[0], (materials.getKd(nextMtlName))[1], (materials.getKd(nextMtlName))[2], (materials.getd(nextMtlName)));

                    if (mtlCount < totalMtls) {
                        nextMtlNameArr = mtlTimings.get(mtlCount);
                        nextMtlName = nextMtlNameArr[0];
                        nextMtl = Integer.parseInt(nextMtlNameArr[1]);
                    }
                    mtlCount++;
                }

                int[] tempFaces = faces.get(j);
                int[] tempFacesNorms = faceNorms.get(j);
                int[] tempFacesTextures = faceTextures.get(j);

                //// Quad Begin Header ////
                int polyType;
                if (tempFaces.length == 3) {
                    polyType = GL2.GL_TRIANGLES;
                } else if (tempFaces.length == 4) {
                    polyType = GL2.GL_QUADS;
                } else {
                    polyType = GL2.GL_POLYGON;
                }
                glTextures.get(i).bind(gl);
                gl.glBegin(polyType);
                ////////////////////////////

                for (int w = 0; w < tempFaces.length; w++) {
                    // define material properties
                    // oh my god this guy's atrocious code makes me write more atrocious code. vicious cycles...
                    MtlLoader.mtl mat = (MtlLoader.mtl) materials.Materials.get(i);

                    float[] matKa = mat.Ka;
                    float[] matKd = mat.Kd;
                    float[] matKs = mat.Ks;

                    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, FloatBuffer.wrap(matKa));
                    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, FloatBuffer.wrap(matKd));
                    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, FloatBuffer.wrap(matKs));

                    if (tempFacesNorms[w] != 0) {
                        double tempNormX = vertSetNorms.get(tempFacesNorms[w] - 1)[0];
                        double tempNormY = vertSetNorms.get(tempFacesNorms[w] - 1)[1];
                        double tempNormZ = vertSetNorms.get(tempFacesNorms[w] - 1)[2];
                        gl.glNormal3d(tempNormX, tempNormY, tempNormZ);
                    }

                    if (tempFacesTextures[w] != 0) {
                        double tempTextureX = vertSetTextures.get(tempFacesTextures[w] - 1)[0];
                        double tempTextureY = vertSetTextures.get(tempFacesTextures[w] - 1)[1];
                        gl.glTexCoord2d(tempTextureX, tempTextureY);
                    }

                    double tempX = vertSets.get(tempFaces[w] - 1)[0];
                    double tempY = vertSets.get(tempFaces[w] - 1)[1];
                    double tempZ = vertSets.get(tempFaces[w] - 1)[2];
                    gl.glVertex3d(tempX,tempY,tempZ);
                }


                //// Quad End Footer /////
                gl.glEnd();
                ///////////////////////////


            }
            gl.glEndList();
        }
    }

    private BufferedImage readPng(String resName) {
        try{
            URL url = new URL("file", "localhost", resName);

            BufferedImage img = ImageIO.read(url);

            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -img.getHeight(null));

            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            img = op.filter(img, null);

            return img;
        } catch (MalformedURLException e) {
            System.err.println("Couldn't find " + resName + " in the expected directory.");
            e.getMessage();
            e.printStackTrace();

            return null;
        } catch (IOException e) {
            System.err.println("Couldn't load " + resName + " in the expected directory.");
            e.getMessage();
            e.printStackTrace();

            return null;
        }
    }

    private void makeRgbTexture(GL gl, GLU glu, BufferedImage img, int target, boolean mipmapped) {
        ByteBuffer buffer;
        switch(img.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_CUSTOM:
                byte[] bData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                buffer = ByteBuffer.allocateDirect(bData.length);
                buffer.put(bData, 0, bData.length);
                break;

            case BufferedImage.TYPE_INT_RGB:
                int[] iData = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                buffer = ByteBuffer.allocateDirect(iData.length * BufferUtil.SIZEOF_INT);
                buffer.order(ByteOrder.nativeOrder());
                buffer.asIntBuffer().put(iData, 0, iData.length);
                break;

            default:
                throw new RuntimeException("Unsupported image type " + img.getType());
        }

        if(mipmapped) {
            glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(), img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);
        }
        else {
            gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(), img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);
        }
    }

    public void draw(GL2 gl){
        for(int i = 0; i < objectLists.size(); i++) {
            glTextures.get(i).enable(gl);
            gl.glCallList(objectLists.get(i));
            if(i == 0) {
                glTextures.get(glTextures.size() - 1).disable(gl);
            }
            else {
                glTextures.get(i - 1).disable(gl);
            }
        }
        if(materials.hasTextureMap) {
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }
    }
}