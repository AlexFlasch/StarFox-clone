import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.sun.prism.impl.BufferUtil;
import jogamp.opengl.glu.mipmap.Image;

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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author wjur on Github
 *
 * Modified by Alex Flasch to use JOGL
 */

public class GLModel{

    private ArrayList vertexsets;
    private ArrayList vertexsetsnorms;
    private ArrayList vertexsetstexs;
    private ArrayList faces;
    private ArrayList facestexs;
    private ArrayList facesnorms;
    private ArrayList mattimings;
    private MtlLoader materials;
    private int objectlist;
    private int numpolys;
    public float toppoint;
    public float bottompoint;
    public float leftpoint;
    public float rightpoint;
    public float farpoint;
    public float nearpoint;
    private String mtl_path;

    GLU glu;

    //THIS CLASS LOADS THE MODELS
    public GLModel(BufferedReader ref, boolean centerit, String path, GL2 gl){
        glu = new GLU();

        mtl_path=path;
        vertexsets = new ArrayList();
        vertexsetsnorms = new ArrayList();
        vertexsetstexs = new ArrayList();
        faces = new ArrayList();
        facestexs = new ArrayList();
        facesnorms = new ArrayList();
        mattimings = new ArrayList();
        numpolys = 0;
        toppoint = 0.0F;
        bottompoint = 0.0F;
        leftpoint = 0.0F;
        rightpoint = 0.0F;
        farpoint = 0.0F;
        nearpoint = 0.0F;
        loadobject(ref);
        if(centerit)
            centerit();
        try{
            opengldrawtolist(gl);
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found.");
            e.getMessage();
        }
        numpolys = faces.size();
        cleanup();
    }

    private void cleanup(){
        vertexsets.clear();
        vertexsetsnorms.clear();
        vertexsetstexs.clear();
        faces.clear();
        facestexs.clear();
        facesnorms.clear();
    }

    private void loadobject(BufferedReader br){
        int linecounter = 0;
        int facecounter = 0;
        try{
            boolean firstpass = true;
            String newline;
            while((newline = br.readLine()) != null){
                linecounter++;
                if(newline.length() > 0){
                    newline = newline.trim();

                    //LOADS VERTEX COORDINATES
                    if(newline.startsWith("v ")){
                        float coords[] = new float[4];
                        String coordstext[] = new String[4];
                        newline = newline.substring(2, newline.length());
                        StringTokenizer st = new StringTokenizer(newline, " ");
                        for(int i = 0; st.hasMoreTokens(); i++)
                            coords[i] = Float.parseFloat(st.nextToken());

                        if(firstpass){
                            rightpoint = coords[0];
                            leftpoint = coords[0];
                            toppoint = coords[1];
                            bottompoint = coords[1];
                            nearpoint = coords[2];
                            farpoint = coords[2];
                            firstpass = false;
                        }
                        if(coords[0] > rightpoint)
                            rightpoint = coords[0];
                        if(coords[0] < leftpoint)
                            leftpoint = coords[0];
                        if(coords[1] > toppoint)
                            toppoint = coords[1];
                        if(coords[1] < bottompoint)
                            bottompoint = coords[1];
                        if(coords[2] > nearpoint)
                            nearpoint = coords[2];
                        if(coords[2] < farpoint)
                            farpoint = coords[2];
                        vertexsets.add(coords);
                    }
                    else {

                        //LOADS VERTEX TEXTURE COORDINATES
                        if (newline.startsWith("vt")) {
                            float coords[] = new float[4];
                            String coordstext[] = new String[4];
                            newline = newline.substring(3, newline.length());
                            StringTokenizer st = new StringTokenizer(newline, " ");
                            for (int i = 0; st.hasMoreTokens(); i++)
                                coords[i] = Float.parseFloat(st.nextToken());

                            vertexsetstexs.add(coords);
                        } else {

                            //LOADS VERTEX NORMALS COORDINATES
                            if (newline.startsWith("vn")) {
                                float coords[] = new float[4];
                                String coordstext[] = new String[4];
                                newline = newline.substring(3, newline.length());
                                StringTokenizer st = new StringTokenizer(newline, " ");
                                for (int i = 0; st.hasMoreTokens(); i++)
                                    coords[i] = Float.parseFloat(st.nextToken());

                                vertexsetsnorms.add(coords);
                            } else {

                                //LOADS FACES COORDINATES
                                if (newline.startsWith("f ")) {
                                    facecounter++;
                                    newline = newline.substring(2, newline.length());
                                    StringTokenizer st = new StringTokenizer(newline, " ");
                                    int count = st.countTokens();
                                    int v[] = new int[count];
                                    int vt[] = new int[count];
                                    int vn[] = new int[count];
                                    for (int i = 0; i < count; i++) {
                                        char chars[] = st.nextToken().toCharArray();
                                        StringBuffer sb = new StringBuffer();
                                        char lc = 'x';
                                        for (int k = 0; k < chars.length; k++) {
                                            if (chars[k] == '/' && lc == '/')
                                                sb.append('0');
                                            lc = chars[k];
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
                                    facestexs.add(vt);
                                    facesnorms.add(vn);
                                } else {

                                    //LOADS MATERIALS
                                    if (newline.startsWith("mtllib")) {
                                        String[] coordstext = new String[3];
                                        coordstext = newline.split("\\s+");
                                        if (mtl_path != null)
                                            loadmaterials();
                                    } else {

                                        //USES MATERIALS
                                        if (newline.startsWith("usemtl")) {
                                            String[] coords = new String[2];
                                            String[] coordstext = new String[3];
                                            coordstext = newline.split("\\s+");
                                            coords[0] = coordstext[1];
                                            coords[1] = facecounter + "";
                                            mattimings.add(coords);
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

    private void loadmaterials() {
        FileReader frm;
        String refm = mtl_path;

        try {
            frm = new FileReader(refm);
            BufferedReader brm = new BufferedReader(frm);
            materials = new MtlLoader(brm,mtl_path);
            frm.close();
        } catch (IOException e) {
            System.out.println("Could not open file: " + refm);
            materials = null;
        }
    }

    private void centerit(){
        float xshift = (rightpoint - leftpoint) / 2.0F;
        float yshift = (toppoint - bottompoint) / 2.0F;
        float zshift = (nearpoint - farpoint) / 2.0F;
        for(int i = 0; i < vertexsets.size(); i++){
            float coords[] = new float[4];
            coords[0] = ((float[])vertexsets.get(i))[0] - leftpoint - xshift;
            coords[1] = ((float[])vertexsets.get(i))[1] - bottompoint - yshift;
            coords[2] = ((float[])vertexsets.get(i))[2] - farpoint - zshift;
            vertexsets.set(i, coords);
        }

    }

    public float getXWidth(){
        float returnval = 0.0F;
        returnval = rightpoint - leftpoint;
        return returnval;
    }

    public float getYHeight(){
        float returnval = 0.0F;
        returnval = toppoint - bottompoint;
        return returnval;
    }

    public float getZDepth(){
        float returnval = 0.0F;
        returnval = nearpoint - farpoint;
        return returnval;
    }

    public int numpolygons(){
        return numpolys;
    }

    public void opengldrawtolist(GL2 gl) throws FileNotFoundException {
        ////////////////////////////////////////
        /// With Materials if available ////////
        ////////////////////////////////////////
        this.objectlist = gl.glGenLists(1);

        int nextmat = -1;
        int matcount = 0;
        int totalmats = mattimings.size();
        String[] nextmatnamearray = null;
        String nextmatname = null;

        if (totalmats > 0 && materials != null) {
            nextmatnamearray = (String[])(mattimings.get(matcount));
            nextmatname = nextmatnamearray[0];
            nextmat = Integer.parseInt(nextmatnamearray[1]);
        }

        gl.glNewList(objectlist,GL2.GL_COMPILE);
        for (int i=0;i<faces.size();i++) {
            if (i == nextmat) {
                if(materials.hasTextureMap) {
                    int kaTexture;
                    int kdTexture;
                    int ksTexture;

                    gl.glEnable(GL2.GL_TEXTURE_2D);

                    String[] temp;
                    temp = Arrays.copyOf(mtl_path.split("/"), mtl_path.split("/").length - 1);
                    String resPath = new String();
                    StringBuilder sb = new StringBuilder();

                    for (String s : temp) {
                        sb.append(s + "/");
                    }

                    resPath = sb.toString();

                    try {
                        URL textureUrl = new URL("file", "localhost", (resPath + nextmatname + ".png"));

                        if (materials.kaTexturePath != null) {
                            kaTexture = genTexture(gl);
                            gl.glBindTexture(GL.GL_TEXTURE_2D, kaTexture);
                            BufferedImage img = readPng(resPath + nextmatname + ".png");
                            if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                                TextureIO.newTexture(textureUrl, true, null);
                            } else {
                                makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                            }
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                        }

                        if (materials.kdTexturePath != null) {
                            kdTexture = genTexture(gl);
                            gl.glBindTexture(GL.GL_TEXTURE_2D, kdTexture);
                            BufferedImage img = readPng(resPath + nextmatname + ".png");
                            if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                                TextureIO.newTexture(textureUrl, true, null);
                            } else {
                                makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                            }
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                        }

                        if (materials.ksTexturePath != null) {
                            ksTexture = genTexture(gl);
                            gl.glBindTexture(GL.GL_TEXTURE_2D, ksTexture);
                            BufferedImage img = readPng(resPath + nextmatname + ".png");
                            if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                                TextureIO.newTexture(textureUrl, true, null);
                            } else {
                                makeRgbTexture(gl, glu, img, GL.GL_TEXTURE_2D, false);
                            }
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                        }

                    } catch (MalformedURLException e) {

                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    gl.glEnable(GL2.GL_COLOR_MATERIAL);
                    gl.glColor4f((materials.getKd(nextmatname))[0], (materials.getKd(nextmatname))[1], (materials.getKd(nextmatname))[2], (materials.getd(nextmatname)));
                }
                matcount++;
                if (matcount < totalmats) {
                    nextmatnamearray = (String[])(mattimings.get(matcount));
                    nextmatname = nextmatnamearray[0];
                    nextmat = Integer.parseInt(nextmatnamearray[1]);
                }
            }

            int[] tempfaces = (int[])(faces.get(i));
            int[] tempfacesnorms = (int[])(facesnorms.get(i));
            int[] tempfacestexs = (int[])(facestexs.get(i));

            //// Quad Begin Header ////
            int polytype;
            if (tempfaces.length == 3) {
                polytype = gl.GL_TRIANGLES;
            } else if (tempfaces.length == 4) {
                polytype = gl.GL_QUADS;
            } else {
                polytype = gl.GL_POLYGON;
            }
            gl.glBegin(polytype);
            ////////////////////////////

            for (int w=0;w<tempfaces.length;w++) {
                if (tempfacesnorms[w] != 0) {
                    float normtempx = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[0];
                    float normtempy = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[1];
                    float normtempz = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[2];
                    gl.glNormal3f(normtempx, normtempy, normtempz);
                }

                if (tempfacestexs[w] != 0) {
                    float textempx = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[0];
                    float textempy = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[1];
                    float textempz = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[2];
                    gl.glTexCoord3f(textempx,1f-textempy,textempz);
                }

                float tempx = ((float[])vertexsets.get(tempfaces[w] - 1))[0];
                float tempy = ((float[])vertexsets.get(tempfaces[w] - 1))[1];
                float tempz = ((float[])vertexsets.get(tempfaces[w] - 1))[2];
                gl.glVertex3f(tempx,tempy,tempz);
            }


            //// Quad End Footer /////
            gl.glEnd();
            ///////////////////////////


        }
        gl.glEndList();
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
        ByteBuffer destination = null;
        int temp = img.getType();
        switch(img.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_CUSTOM:
                byte[] bData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                destination = ByteBuffer.allocateDirect(bData.length);
                destination.put(bData, 0, bData.length);
                break;

            case BufferedImage.TYPE_INT_RGB:
                int[] iData = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                destination = ByteBuffer.allocateDirect(iData.length * BufferUtil.SIZEOF_INT);
                destination.order(ByteOrder.nativeOrder());
                destination.asIntBuffer().put(iData, 0, iData.length);
                break;

            default:
                throw new RuntimeException("Unsupported image type " + img.getType());
        }

        if(mipmapped) {
            glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(), img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, destination);
        }
        else {
            gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(), img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, destination);
        }
    }

    private int genTexture(GL gl) {
        final int[] temp = new int[1];
        gl.glGenTextures(1, temp, 0);
        return temp[0];
    }

    public void opengldraw(GL2 gl){
        gl.glCallList(objectlist);
        gl.glDisable(GL2.GL_COLOR_MATERIAL);
    }
}