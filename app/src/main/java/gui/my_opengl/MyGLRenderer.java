package gui.my_opengl;


import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import gui.draw_class.MyColorClass;
import gui.my_opengl.dozer.GL_DrawDozer;
import gui.my_opengl.exca.GL_DrawExca;
import gui.my_opengl.wheel.GL_DrawWheel;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import services.TriangleService;
import utils.MyData;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    public static float scale;
    public static float angleX;
    public static float angleY;
    public static float panX;
    public static float panY;
    private boolean isXML, isXMLPoint;
    public static float[] coloreEsterno = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreInterno = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreAttacco = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreAttaccoScuro = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreBoom = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreBoomScuro = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static FontAtlas atlas;
    public static float charSpacingFactor = 0.5f;//spaziatura fra i caratteri testo

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (gl instanceof GL11) {
            GL11 gl11 = (GL11) gl;
            try {
                atlas = new FontAtlas(gl11, 32, MyColorClass.colorConstraint);


                if (MyData.get_String("colorBucket") == null) {
                    MyData.push("colorBucket", String.valueOf(Color.DKGRAY));
                }
                if (MyData.get_String("colorBoom") == null) {
                    MyData.push("colorBoom", String.valueOf(Color.CYAN));

                }
                if (MyData.get_String("colorQuick") == null) {
                    MyData.push("colorQuick", String.valueOf(Color.GRAY));

                }

                scale = MyData.get_Float("glScale");
                angleX = MyData.get_Float("glAngleX");
                angleY = MyData.get_Float("glAngleY");
                panX = 0;
                panY = -0.3f;
            } catch (Exception e) {
                scale = 0.5f;
                angleX = -90f;
                angleY = 0f;
                panX = 0f;
                panY = -0.3f;

            }
            try {
                isXML = DataSaved.progettoSelected.substring(DataSaved.progettoSelected.lastIndexOf(".") + 1).equalsIgnoreCase("xml");


            } catch (Exception e) {
                isXML = false;
            }
            try {
                isXMLPoint = DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

            } catch (Exception e) {
                isXMLPoint = false;
            }
            // Colore di sfondo (grigio scuro)
            float[] sf = GL_Methods.parseColorToGL(MyColorClass.colorSfondo);
            gl.glClearColor(sf[0], sf[1], sf[2], sf[3]);

            // Abilita test di profondità per disegno corretto degli oggetti 3D
            gl11.glEnable(GL11.GL_DEPTH_TEST);
            gl11.glDepthFunc(GL11.GL_LEQUAL);

            // Abilita blending per trasparenze (alpha)
            gl11.glEnable(GL11.GL_BLEND);
            gl11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // Altre opzioni possibili
            gl11.glShadeModel(GL11.GL_SMOOTH);
            gl11.glEnable(GL11.GL_LINE_SMOOTH);
            gl11.glEnable(GL10.GL_NICEST);
            gl11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        }

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (gl instanceof GL11) {
            GL11 gl11 = (GL11) gl;
            coloreEsterno = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyColorClass.colorBucket), 1, 1);

            //coloreEsterno = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyData.get_Int("colorBucket")), 1, 1);
            coloreInterno = GL_Methods.darkenColor(coloreEsterno, 0.5f, 0.95f);

            coloreAttacco = GL_Methods.darkenColor(GL_Methods.parseColorToGL(Color.GRAY), 1, 1);

            //coloreAttacco = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyData.get_Int("colorQuick")), 1, 1);
            coloreAttaccoScuro = GL_Methods.darkenColor(coloreAttacco, 0.75f, 1f);

            coloreBoom = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyColorClass.colorStick), 1, 1);
            //coloreBoom = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyData.get_Int("colorBoom")), 1, 1);
            coloreBoomScuro = GL_Methods.darkenColor(coloreBoom, 0.75f, 1f);
            gl11.glViewport(0, 0, width, height);

            gl11.glMatrixMode(GL11.GL_PROJECTION);
            gl11.glLoadIdentity();

            float ratio = (float) width / height;

            // Usa la prospettiva con un campo visivo realistico e profondità adeguata
            GLU.gluPerspective(gl, 45.0f, ratio, 0.1f, 100.0f);

            gl11.glMatrixMode(GL11.GL_MODELVIEW);
            gl11.glLoadIdentity();
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        if (gl instanceof GL11) {
            GL11 gl11 = (GL11) gl;
            if (DataSaved.typeView == 0 || DataSaved.typeView == 1) {
                try {

                    float angleTest = (float) ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360);

                    gl11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    gl11.glLoadIdentity();

                    if (My3DActivity.glVista3d) {
                        gl11.glTranslatef(panX, panY, -5.0f);  // Pan in 3D
                        gl11.glScalef(scale, scale, scale);   // Scala in tutte le direzioni
                        //in 3D ruota la scena benna ancorata a 0,0
                        gl11.glRotatef(angleX, 1f, 0f, 0f);
                        gl11.glRotatef(angleY, 0f, 0f, 1f);

                    } else {

                        gl11.glTranslatef(panX, panY, -5f);  // Pan in 3D
                        gl11.glScalef(scale, scale, 0);   // non scala Z
                        //in 2D ruota il terreno e non la macchina
                        gl11.glRotatef(0, 1f, 0f, 0f);
                        gl11.glRotatef(angleTest, 0f, 0f, 1f);

                    }

                    if (!My3DActivity.glFilter) {
                        //dati non filtrati
                        if (My3DActivity.glVista3d) {
                            //tutto in 3D
                            if (My3DActivity.glFace || My3DActivity.glFill) {
                                GLDrawer.drawFaces(gl11, DataSaved.dxfFaces, 0.8f, scale, isXML);//disegna le 3DFaces
                            }
                            if (My3DActivity.glGradient) {
                                GLDrawer.drawFacesGradient(gl11, DataSaved.dxfFaces, scale, TriangleService.minZ, TriangleService.maxZ);
                            }
                            gl.glDisable(GL10.GL_DEPTH_TEST);
                            if (My3DActivity.glPoly) {
                                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, scale);
                            }
                            if (My3DActivity.glPoint) {
                                GLDrawer.drawPoints(gl11, DataSaved.points, 10f, scale, isXMLPoint);
                            }
                            if (My3DActivity.glText) {
                                GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
                            }
                            gl.glEnable(GL11.GL_DEPTH_TEST);


                        } else {
                            //tutto Z a 0 2D
                            if (My3DActivity.glFace || My3DActivity.glFill) {
                                Log.d("facce2D", String.valueOf(DataSaved.dxfFacesGL_2D.size()));
                                GLDrawer.drawFaces(gl11, DataSaved.dxfFacesGL_2D, 0.8f, scale, isXML);//disegna le 3DFaces
                            }
                            if (My3DActivity.glGradient) {
                                GLDrawer.drawFacesGradient2D(gl11, DataSaved.dxfFaces, scale, TriangleService.minZ, TriangleService.maxZ);
                            }
                            gl.glDisable(GL10.GL_DEPTH_TEST);
                            if (My3DActivity.glPoly) {
                                Log.d("poly2D", String.valueOf(DataSaved.polylinesGL_2D.size()));
                                GLDrawer.drawPolylines(gl11, DataSaved.polylinesGL_2D, 3f, scale);
                                //altre entità 2D dxf
                                GLDrawer.drawLines2D(gl11, DataSaved.lines_2D, 3f, scale);
                                GLDrawer.drawArcs2D(gl11, DataSaved.arcs, 2f, scale);
                                GLDrawer.drawPolylines2D(gl11, DataSaved.polylines_2D, 3f, scale);
                                GLDrawer.drawCircles2D(gl11, DataSaved.circles, 2f, scale);
                            }
                            if (My3DActivity.glPoint) {
                                GLDrawer.drawPoints(gl11, DataSaved.points, 10f, scale, isXMLPoint);
                            }
                            if (My3DActivity.glText) {
                                GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
                            }
                            gl.glEnable(GL11.GL_DEPTH_TEST);

                        }
                    } else {
                        //filtro di distanza applicato
                        if (My3DActivity.glVista3d) {
                            //tutto in 3D
                            if (My3DActivity.glFace || My3DActivity.glFill) {
                                GLDrawer.drawFaces(gl11, DataSaved.filteredFaces, 0.8f, scale, isXML);//disegna le 3DFaces
                            }
                            if (My3DActivity.glGradient) {
                                GLDrawer.drawFacesGradient(gl11, DataSaved.filteredFaces, scale, TriangleService.minZ, TriangleService.maxZ);
                            }
                            gl11.glDisable(GL10.GL_DEPTH_TEST);
                            if (My3DActivity.glPoly) {
                                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, scale);
                            }
                            if (My3DActivity.glPoint) {
                                GLDrawer.drawPoints(gl11, DataSaved.filteredPoints, 10f, scale, isXMLPoint);
                            }
                            if (My3DActivity.glText) {
                                GLDrawer.drawTextsBilBoard(gl11, DataSaved.filteredDxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
                            }

                            gl11.glEnable(GL10.GL_DEPTH_TEST);


                        } else {
                            //tutto Z a 0
                            if (My3DActivity.glFace || My3DActivity.glFill) {
                                Log.d("facce2D", String.valueOf(DataSaved.dxfFacesGL_2D.size()));
                                GLDrawer.drawFaces(gl11, DataSaved.filteredFacesGL_2D, 0.8f, scale, isXML);//disegna le 3DFaces
                            }
                            if (My3DActivity.glGradient) {
                                GLDrawer.drawFacesGradient2D(gl11, DataSaved.filteredFaces, scale, TriangleService.minZ, TriangleService.maxZ);
                            }
                            gl11.glDisable(GL10.GL_DEPTH_TEST);
                            if (My3DActivity.glPoly) {
                                GLDrawer.drawPolylines(gl11, DataSaved.polylinesGL_2D, 3f, scale);
                                //altre entità 2D dxf
                                GLDrawer.drawLines2D(gl11, DataSaved.lines_2D, 3f, scale);
                                GLDrawer.drawArcs2D(gl11, DataSaved.arcs, 2f, scale);
                                GLDrawer.drawPolylines2D(gl11, DataSaved.polylines_2D, 3f, scale);
                                GLDrawer.drawCircles2D(gl11, DataSaved.circles, 2f, scale);

                            }
                            if (My3DActivity.glPoint) {
                                GLDrawer.drawPoints(gl11, DataSaved.filteredPoints, 10f, scale, isXMLPoint);
                            }
                            if (My3DActivity.glText) {
                                GLDrawer.drawTextsBilBoard(gl11, DataSaved.filteredDxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
                            }
                            gl11.glEnable(GL10.GL_DEPTH_TEST);


                        }

                    }

                    switch (DataSaved.isWL) {
                        case 0:
                            GL_DrawExca.draw(gl11);
                            break;
                        case 1:
                            GL_DrawWheel.draw(gl11);
                            break;
                        case 2:
                        case 3:
                        case 4:
                            GL_DrawDozer.draw(gl11);
                            break;


                    }


                } catch (Exception e) {
                    Log.e("ErrorGLRender", Log.getStackTraceString(e));
                }
            }
        }


    }


}
