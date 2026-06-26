package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.coloreAttacco;
import static gui.my_opengl.MyGLRenderer.coloreAttaccoScuro;
import static gui.my_opengl.MyGLRenderer.coloreBoom;
import static gui.my_opengl.MyGLRenderer.coloreBoomScuro;
import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.Sensors_Decoder_Dredge.Angolo_Attrezzo_Dredge;

import android.opengl.GLES20;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DredgeLib;
import packexcalib.exca.ExcavatorLib;

public class My_DredgeCrane {
    private static final float MAIN_TUBE_RADIUS_M = 0.060f;
    private static final float BRACE_TUBE_RADIUS_M = 0.032f;
    private static final float ROPE_RADIUS_M = 0.030f;

    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static void draw(GL11 gl) {
        draw(gl, true);
    }

    public static void draw(GL11 gl, boolean drawFallbackMachine) {
        Point3DF center = worldToGL(DredgeLib.centroRalla);
        Point3DF root = worldToGL(ExcavatorLib.coordinateDY);
        Point3DF tip = worldToGL(ExcavatorLib.coordB1);
        Point3DF ropeEnd = worldToGL(ExcavatorLib.coordST);
        Point3DF toolEnd = worldToGL(ExcavatorLib.bucketCoord);

        if (!valid(root) || !valid(tip)) return;
        if (!valid(center)) center = root;

        Axes axes = computeAxes(center, root, tip);

        if (DataSaved.drwaMachieSchema > 0 && drawFallbackMachine) {
            // Usato solo quando DataSaved.GL_FRAME_BASE non esiste.
            // Quando GL_FRAME_BASE e disponibile, cabina/frame/carro vengono
            // disegnati da GL_DrawExca con la geometria originale dell'escavatore.
            drawUpperWorks(gl, center, root, axes);
            if (!My_Pontone.hasPontone()) {
                drawCrawlerFallback(gl, center, root, axes);
            }
        }

        // Parte che sostituisce boom/stick/benna escavatore.
        drawAFrameAndBackstays(gl, center, root, tip, axes);
        drawLatticeBoom(gl, root, tip, axes);

        if (valid(ropeEnd)) {
            drawRope(gl, tip, ropeEnd);
        }

        if (valid(ropeEnd) && valid(toolEnd)) {
            drawClamshellTool(gl, ropeEnd, toolEnd, axes);
        }
    }

    private static Axes computeAxes(Point3DF center, Point3DF root, Point3DF tip) {
        Point3DF up = new Point3DF(0f, 0f, 1f);

        /*
         * TUTTA la parte draga deve usare lo stesso heading del boom.
         * Prima usavamo root-center come prima scelta: quando il centro ralla
         * non era perfettamente allineato al boom, il telaio posteriore e il
         * tool ruotavano diversamente dal traliccio.
         *
         * Ora l'asse principale e sempre root->tip, cioe la direzione reale
         * del boom a traliccio nel GL.
         */
        Point3DF forward = flatten(tip.subtract(root));
        if (forward.length() < 0.001f) {
            forward = headingToForward(ExcavatorLib.hdt_BOOM);
        }
        if (forward.length() < 0.001f) {
            forward = new Point3DF(0f, 1f, 0f);
        }
        forward = forward.normalize();

        Point3DF side = up.cross(forward).normalize();
        if (side.length() < 0.001f) {
            side = new Point3DF(1f, 0f, 0f);
        }

        return new Axes(forward, side, up);
    }

    private static Point3DF headingToForward(double headingDeg) {
        double rad = Math.toRadians(headingDeg);
        return new Point3DF((float) Math.sin(rad), (float) Math.cos(rad), 0f);
    }

    private static void drawUpperWorks(GL11 gl, Point3DF center, Point3DF root, Axes ax) {
        float scale = rs();
        float pontoneLen = (float) Math.max(0d, DataSaved.Lunghezza_Pontone * scale);
        float pontoneW = (float) Math.max(0d, DataSaved.Larghezza_Pontone * scale);
        float boomLen = (float) Math.max(4d * scale, DataSaved.Lunghezza_Braccio * scale);

        float deckLength = clamp(pontoneLen > 0f ? pontoneLen * 0.28f : boomLen * 0.24f, 1.4f * scale, 4.5f * scale);
        float deckWidth = clamp(pontoneW > 0f ? pontoneW * 0.42f : deckLength * 0.82f, 1.0f * scale, 2.7f * scale);
        float deckHeight = clamp(boomLen * 0.06f, 0.45f * scale, 0.90f * scale);

        Point3DF deckCenter = center.add(ax.up.scale(deckHeight * 0.5f));
        Point3DF[] deck = makeBox(deckCenter, ax.forward, ax.side, ax.up, deckLength, deckWidth, deckHeight);
        drawSolid(gl, deck, coloreBoom, coloreBoomScuro, boxLight(), boxDark(), boxEdges());

        float bodyLength = deckLength * 0.62f;
        float bodyWidth = deckWidth * 0.88f;
        float bodyHeight = deckHeight * 0.95f;
        Point3DF bodyCenter = deckCenter.add(ax.up.scale((deckHeight + bodyHeight) * 0.48f)).subtract(ax.forward.scale(deckLength * 0.05f));
        Point3DF[] body = makeBox(bodyCenter, ax.forward, ax.side, ax.up, bodyLength, bodyWidth, bodyHeight);
        drawSolid(gl, body, coloreBoom, coloreBoomScuro, boxLight(), boxDark(), boxEdges());

        float cwLength = bodyLength * 0.38f;
        float cwWidth = bodyWidth * 0.96f;
        float cwHeight = bodyHeight * 0.68f;
        Point3DF counterCenter = bodyCenter.subtract(ax.forward.scale(bodyLength * 0.32f)).add(ax.up.scale(cwHeight * 0.08f));
        Point3DF[] counter = makeBox(counterCenter, ax.forward, ax.side, ax.up, cwLength, cwWidth, cwHeight);
        drawSolid(gl, counter, coloreAttacco, coloreAttaccoScuro, boxLight(), boxDark(), boxEdges());

        float cabLength = bodyLength * 0.36f;
        float cabWidth = bodyWidth * 0.34f;
        float cabHeight = bodyHeight * 0.95f;
        Point3DF cabinCenter = bodyCenter
                .add(ax.forward.scale(bodyLength * 0.20f))
                .add(ax.side.scale(bodyWidth * 0.26f))
                .add(ax.up.scale(cabHeight * 0.14f));
        Point3DF[] cabin = makeBox(cabinCenter, ax.forward, ax.side, ax.up, cabLength, cabWidth, cabHeight);
        drawSolid(gl, cabin, coloreAttacco, coloreAttaccoScuro, boxLight(), boxDark(), boxEdges());

        float pedestalLength = bodyLength * 0.22f;
        float pedestalWidth = bodyWidth * 0.24f;
        float pedestalHeight = bodyHeight * 0.75f;
        Point3DF pedCenter = midpoint(center, root)
                .add(ax.up.scale(pedestalHeight * 0.5f))
                .add(ax.forward.scale(pedestalLength * 0.08f));
        Point3DF[] pedestal = makeBox(pedCenter, ax.forward, ax.side, ax.up, pedestalLength, pedestalWidth, pedestalHeight);
        drawSolid(gl, pedestal, coloreBoom, coloreBoomScuro, boxLight(), boxDark(), boxEdges());

        // perno ralla / colonna centrale
        float columnRadius = clamp(deckWidth * 0.10f, 0.10f * scale, 0.28f * scale);
        drawTube(gl,
                center.add(ax.up.scale(0.02f * scale)),
                center.add(ax.up.scale(deckHeight + bodyHeight * 0.15f)),
                columnRadius,
                coloreAttaccoScuro,
                12);
    }

    private static void drawCrawlerFallback(GL11 gl, Point3DF center, Point3DF root, Axes ax) {
        float scale = rs();
        float boomLen = (float) Math.max(4.0d, DataSaved.Lunghezza_Braccio * scale);

        float trackLength = clamp(boomLen * 0.26f, 1.60f * scale, 4.50f * scale);
        float trackWidth = clamp(trackLength * 0.18f, 0.28f * scale, 0.75f * scale);
        float trackHeight = clamp(trackLength * 0.13f, 0.22f * scale, 0.55f * scale);
        float trackSpacing = clamp(trackLength * 0.28f, 0.75f * scale, 1.75f * scale);

        Point3DF baseCenter = center.subtract(ax.up.scale(trackHeight * 0.52f));
        Point3DF leftCenter = baseCenter.subtract(ax.side.scale(trackSpacing * 0.5f));
        Point3DF rightCenter = baseCenter.add(ax.side.scale(trackSpacing * 0.5f));

        Point3DF[] leftTrack = makeBox(leftCenter, ax.forward, ax.side, ax.up,
                trackLength, trackWidth, trackHeight);
        Point3DF[] rightTrack = makeBox(rightCenter, ax.forward, ax.side, ax.up,
                trackLength, trackWidth, trackHeight);

        drawSolid(gl, leftTrack, coloreAttacco, coloreAttaccoScuro, boxLight(), boxDark(), boxEdges());
        drawSolid(gl, rightTrack, coloreAttacco, coloreAttaccoScuro, boxLight(), boxDark(), boxEdges());

        // traversa centrale tra i due cingoli, cosi il carro non sembra staccato.
        float crossLength = clamp(trackLength * 0.45f, 0.75f * scale, 1.80f * scale);
        float crossWidth = Math.max(trackSpacing - trackWidth, trackWidth * 0.85f);
        float crossHeight = trackHeight * 0.55f;
        Point3DF crossCenter = baseCenter.add(ax.up.scale(trackHeight * 0.30f));
        Point3DF[] cross = makeBox(crossCenter, ax.forward, ax.side, ax.up,
                crossLength, crossWidth, crossHeight);
        drawSolid(gl, cross, coloreBoomScuro, coloreBoomScuro, boxLight(), boxDark(), boxEdges());
    }

    private static void drawAFrameAndBackstays(GL11 gl, Point3DF center, Point3DF root, Point3DF tip, Axes ax) {
        float scale = rs();
        float boomLen = Math.max(tip.subtract(root).length(), 1f);
        float baseSpread = clamp(boomLen * 0.12f, 0.35f * scale, 1.10f * scale);
        float mastHeight = clamp(boomLen * 0.28f, 0.90f * scale, 3.50f * scale);

        // Il telaio/falcone di tenuta fune deve stare a fine frame, non sotto il boom.
        // Uso prima il tetto/fine della zavorra del frame standard.
        // Se quei punti non esistono, ripiego su FINE_ZAV o su un fallback geometrico.
        Point3DF mastBase = aFrameBaseOnRearFrame(center, root, ax, boomLen);
        mastBase = mastBase.add(ax.up.scale(0.08f * scale));
        Point3DF mastTop = mastBase.add(ax.up.scale(mastHeight));

        Point3DF leftBase = mastBase.subtract(ax.side.scale(baseSpread));
        Point3DF rightBase = mastBase.add(ax.side.scale(baseSpread));

        float mainRadius = Math.max(MAIN_TUBE_RADIUS_M * scale, 0.02f);
        float braceRadius = Math.max(BRACE_TUBE_RADIUS_M * scale, 0.012f);

        drawTube(gl, leftBase, mastTop, mainRadius, coloreBoom, 10);
        drawTube(gl, rightBase, mastTop, mainRadius, coloreBoom, 10);
        drawTube(gl, leftBase, rightBase, mainRadius, coloreBoomScuro, 10);

        // Brevi controventi sul tetto/fine frame. Non li porto piu fino al perno boom,
        // altrimenti il triangolo torna visivamente sotto al braccio.
        float footForward = clamp(boomLen * 0.06f, 0.25f * scale, 0.85f * scale);
        Point3DF frontLeftFoot = mastBase.add(ax.forward.scale(footForward)).subtract(ax.side.scale(baseSpread * 0.45f));
        Point3DF frontRightFoot = mastBase.add(ax.forward.scale(footForward)).add(ax.side.scale(baseSpread * 0.45f));
        drawTube(gl, leftBase, frontLeftFoot, braceRadius, coloreBoomScuro, 8);
        drawTube(gl, rightBase, frontRightFoot, braceRadius, coloreBoomScuro, 8);
        drawTube(gl, frontLeftFoot, frontRightFoot, braceRadius, coloreBoomScuro, 8);

        // Backstay ropes come nella gru reale: dal telaio posteriore alla testa traliccio.
        Point3DF tipLeft = tip.subtract(ax.side.scale(baseSpread * 0.18f));
        Point3DF tipRight = tip.add(ax.side.scale(baseSpread * 0.18f));
        drawTube(gl, mastTop, tipLeft, Math.max(ROPE_RADIUS_M * scale, 0.005f), coloreAttaccoScuro, 6);
        drawTube(gl, mastTop, tipRight, Math.max(ROPE_RADIUS_M * scale, 0.005f), coloreAttaccoScuro, 6);
    }

    private static Point3DF aFrameBaseOnRearFrame(Point3DF center, Point3DF root, Axes ax, float boomLen) {
        if (hasFramePoint(30) && hasFramePoint(31) && hasFramePoint(32) && hasFramePoint(33)) {
            return average4(
                    DataSaved.GL_FRAME_BASE[30],
                    DataSaved.GL_FRAME_BASE[31],
                    DataSaved.GL_FRAME_BASE[32],
                    DataSaved.GL_FRAME_BASE[33]
            );
        }
        if (hasFramePoint(34)) {
            return DataSaved.GL_FRAME_BASE[34];
        }
        if (hasFramePoint(26) && hasFramePoint(29)) {
            return midpoint(DataSaved.GL_FRAME_BASE[26], DataSaved.GL_FRAME_BASE[29]);
        }

        float aftOffset = clamp(boomLen * 0.16f, 0.40f * rs(), 1.50f * rs());
        return center.subtract(ax.forward.scale(aftOffset));
    }

    private static boolean hasFramePoint(int index) {
        return DataSaved.GL_FRAME_BASE != null
                && DataSaved.GL_FRAME_BASE.length > index
                && DataSaved.GL_FRAME_BASE[index] != null;
    }

    private static void drawLatticeBoom(GL11 gl, Point3DF root, Point3DF tip, Axes baseAxes) {
        Point3DF boomAxis = tip.subtract(root).normalize();
        if (boomAxis.length() < 0.001f) return;

        Point3DF sideAxis = baseAxes.up.cross(boomAxis).normalize();
        if (sideAxis.length() < 0.001f) {
            sideAxis = baseAxes.side;
        }
        Point3DF depthAxis = boomAxis.cross(sideAxis).normalize();
        if (depthAxis.length() < 0.001f) {
            depthAxis = baseAxes.up;
        }

        float scale = rs();
        float boomLen = tip.subtract(root).length();
        float widthBase = clamp(boomLen * 0.10f, 0.40f * scale, 1.45f * scale);
        float widthTip = clamp(widthBase * 0.42f, 0.18f * scale, 0.65f * scale);
        float depthBase = widthBase * 0.55f;
        float depthTip = widthTip * 0.55f;
        float rMain = Math.max(MAIN_TUBE_RADIUS_M * scale, 0.02f);
        float rBrace = Math.max(BRACE_TUBE_RADIUS_M * scale, 0.010f);

        Section s0 = section(root, sideAxis, depthAxis, widthBase, depthBase);
        Section s1 = section(tip, sideAxis, depthAxis, widthTip, depthTip);

        // longherons
        drawTube(gl, s0.topLeft, s1.topLeft, rMain, coloreBoom, 10);
        drawTube(gl, s0.topRight, s1.topRight, rMain, coloreBoom, 10);
        drawTube(gl, s0.bottomLeft, s1.bottomLeft, rMain, coloreBoomScuro, 10);
        drawTube(gl, s0.bottomRight, s1.bottomRight, rMain, coloreBoomScuro, 10);

        drawSection(gl, s0, rMain);
        drawSection(gl, s1, rMain);

        int segments = clamp((int) (boomLen / Math.max(0.55f * scale, 0.55f)), 5, 16);
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / (float) segments;
            float t1 = (float) (i + 1) / (float) segments;
            Section a = sectionAt(root, tip, sideAxis, depthAxis, widthBase, depthBase, widthTip, depthTip, t0);
            Section b = sectionAt(root, tip, sideAxis, depthAxis, widthBase, depthBase, widthTip, depthTip, t1);
            drawSection(gl, b, rBrace * 0.85f);

            drawTube(gl, a.topLeft, b.topRight, rBrace, coloreBoomScuro, 8);
            drawTube(gl, a.bottomLeft, b.bottomRight, rBrace, coloreBoomScuro, 8);
            drawTube(gl, a.topRight, b.topLeft, rBrace, coloreBoomScuro, 8);
            drawTube(gl, a.bottomRight, b.bottomLeft, rBrace, coloreBoomScuro, 8);

            if ((i % 2) == 0) {
                drawTube(gl, a.topLeft, b.bottomLeft, rBrace, coloreBoomScuro, 8);
                drawTube(gl, a.topRight, b.bottomRight, rBrace, coloreBoomScuro, 8);
            } else {
                drawTube(gl, a.bottomLeft, b.topLeft, rBrace, coloreBoomScuro, 8);
                drawTube(gl, a.bottomRight, b.topRight, rBrace, coloreBoomScuro, 8);
            }
        }

        // testa boom con carrucole semplificate
        float pulleyR = clamp(widthTip * 0.22f, 0.06f * scale, 0.18f * scale);
        drawTube(gl,
                s1.topLeft.subtract(boomAxis.scale(pulleyR * 0.5f)),
                s1.topRight.subtract(boomAxis.scale(pulleyR * 0.5f)),
                pulleyR,
                coloreAttaccoScuro,
                12);
    }

    private static void drawSection(GL11 gl, Section s, float radius) {
        drawTube(gl, s.topLeft, s.topRight, radius, coloreBoom, 8);
        drawTube(gl, s.bottomLeft, s.bottomRight, radius, coloreBoomScuro, 8);
        drawTube(gl, s.topLeft, s.bottomLeft, radius, coloreBoomScuro, 8);
        drawTube(gl, s.topRight, s.bottomRight, radius, coloreBoomScuro, 8);
    }

    private static void drawRope(GL11 gl, Point3DF tip, Point3DF ropeEnd) {
        drawTube(gl, tip, ropeEnd, Math.max(ROPE_RADIUS_M * rs(), 0.005f), coloreAttaccoScuro, 6);
    }

    private static void drawClamshellTool(GL11 gl, Point3DF head, Point3DF bottom, Axes machineAxes) {
        Point3DF vertical = bottom.subtract(head).normalize();
        if (vertical.length() < 0.001f) vertical = new Point3DF(0f, 0f, -1f);

        /*
         * Il tool non deve ricavare la sua heading dalla fune: la fune e quasi
         * verticale, quindi non contiene una direzione XY affidabile.
         * Il grab deve invece ereditare sempre gli assi del boom.
         */
        Point3DF lateral = machineAxes.side.normalize();
        if (lateral.length() < 0.001f) lateral = new Point3DF(1f, 0f, 0f);

        Point3DF depthAxis = machineAxes.forward.normalize();
        if (depthAxis.length() < 0.001f) depthAxis = new Point3DF(0f, 1f, 0f);

        float scale = rs();
        float geometricHeight = bottom.subtract(head).length();
        float configuredHeight = (float) (Math.max(0.0d, DataSaved.Altezza_Attrezzo) * scale);
        float boomBasedMin = (float) (Math.max(4.0d, DataSaved.Lunghezza_Braccio) * 0.09d * scale);
        float height = Math.max(geometricHeight, Math.max(configuredHeight, boomBasedMin));
        height = clamp(height, 1.10f * scale, 2.80f * scale);

        /*
         * Proporzione coerente con Layer1/Layer2 Canvas:
         * larghezza massima clamshell = 0.75 * altezza.
         */
        float toolWidth = height * 0.75f;
        float halfToolWidth = toolWidth * 0.50f;

        float halfDepth = clamp(height * 0.24f, 0.16f * scale, 0.62f * scale);
        float halfHeadWidth = clamp(halfToolWidth * 0.48f, 0.12f * scale, 0.42f * scale);
        float halfBellyWidth = clamp(halfToolWidth, 0.26f * scale, 0.95f * scale);
        float halfLipWidth = clamp(halfToolWidth * 0.88f, 0.22f * scale, 0.84f * scale);
        float shellThroat = clamp(height * 0.07f, 0.04f * scale, 0.18f * scale);
        float headBlockH = clamp(height * 0.16f, 0.12f * scale, 0.42f * scale);

        float splitOpen = (float) Math.min(Math.abs(Angolo_Attrezzo_Dredge), 40d) / 40f;
        float split = clamp(height * (0.02f + 0.10f * splitOpen), 0.02f * scale, 0.18f * scale);

        float[] bucketColor = coloreEsterno;
        float[] bucketLineColor = GL_Methods.darkenColor(coloreEsterno.clone(), 0.72f, coloreEsterno[3]);

        // Blocco superiore compatto, da cui partono le due valve.
        Point3DF headBlockTop = head.subtract(vertical.scale(headBlockH * 0.20f));
        Point3DF headBlockBottom = head.add(vertical.scale(headBlockH * 0.80f));
        Point3DF[] headBlock = makeBox(
                midpoint(headBlockTop, headBlockBottom),
                depthAxis,
                lateral,
                vertical.scale(-1f),
                halfDepth * 2.4f,
                halfHeadWidth * 2.3f,
                headBlockH);
        drawSolid(gl, headBlock, bucketColor, bucketColor, boxLight(), boxDark(), boxEdges());

        Point3DF shoulder = head.add(vertical.scale(height * 0.28f));
        Point3DF belly = head.add(vertical.scale(height * 0.66f));
        Point3DF lip = bottom;

        drawGrabShell(gl, true, head, shoulder, belly, lip, lateral, depthAxis,
                halfHeadWidth, halfBellyWidth, halfLipWidth, halfDepth, shellThroat, split, bucketColor);
        drawGrabShell(gl, false, head, shoulder, belly, lip, lateral, depthAxis,
                halfHeadWidth, halfBellyWidth, halfLipWidth, halfDepth, shellThroat, split, bucketColor);

        float pinRadius = clamp(height * 0.025f, 0.018f * scale, 0.055f * scale);
        Point3DF hingeLeft = head.add(lateral.scale(-(halfHeadWidth + split)));
        Point3DF hingeRight = head.add(lateral.scale(halfHeadWidth + split));
        drawTube(gl, hingeLeft, hingeRight, pinRadius, bucketLineColor, 8);
        drawTube(gl, headBlockTop, head, Math.max(pinRadius * 0.60f, 0.010f), bucketLineColor, 8);

        /*
         * Croce di allineamento come nella benna.
         * La disegno sul punto di lavoro del grab, cioè bottom / bucketCoord.
         * Non ricavo la heading dalla fune: uso gli assi già ereditati dal boom.
         */
        if (DataSaved.showAlign > 0) {
            drawToolAlignCross(gl, bottom, vertical, depthAxis, lateral, height, scale);
        }
    }

    private static void drawGrabShell(GL11 gl,
                                      boolean left,
                                      Point3DF head,
                                      Point3DF shoulderCenter,
                                      Point3DF bellyCenter,
                                      Point3DF lipCenter,
                                      Point3DF lateral,
                                      Point3DF depthAxis,
                                      float halfHeadWidth,
                                      float halfBellyWidth,
                                      float halfLipWidth,
                                      float halfDepth,
                                      float shellThroat,
                                      float split,
                                      float[] bucketColor) {
        float sign = left ? -1f : 1f;

        Point3DF hinge = head.add(lateral.scale(sign * (halfHeadWidth + split)));
        Point3DF innerTop = head.add(lateral.scale(sign * (split + shellThroat * 0.25f)));
        Point3DF outerShoulder = shoulderCenter.add(lateral.scale(sign * (halfBellyWidth * 0.68f)));
        Point3DF outerBelly = bellyCenter.add(lateral.scale(sign * halfBellyWidth));
        Point3DF outerLip = lipCenter.add(lateral.scale(sign * halfLipWidth));
        Point3DF innerLip = lipCenter.add(lateral.scale(sign * shellThroat));
        Point3DF innerBelly = bellyCenter.add(lateral.scale(sign * (shellThroat * 1.25f)));
        Point3DF innerShoulder = shoulderCenter.add(lateral.scale(sign * (shellThroat * 0.90f)));

        Point3DF[] pts = new Point3DF[]{
                // faccia davanti
                hinge.add(depthAxis.scale(halfDepth)),          // 0
                outerShoulder.add(depthAxis.scale(halfDepth)),  // 1
                outerBelly.add(depthAxis.scale(halfDepth)),     // 2
                outerLip.add(depthAxis.scale(halfDepth * 0.82f)), // 3
                innerLip.add(depthAxis.scale(halfDepth * 0.55f)),  // 4
                innerBelly.add(depthAxis.scale(halfDepth * 0.62f)), // 5
                innerShoulder.add(depthAxis.scale(halfDepth * 0.78f)), // 6
                innerTop.add(depthAxis.scale(halfDepth * 0.90f)), // 7

                // faccia dietro
                hinge.subtract(depthAxis.scale(halfDepth)),
                outerShoulder.subtract(depthAxis.scale(halfDepth)),
                outerBelly.subtract(depthAxis.scale(halfDepth)),
                outerLip.subtract(depthAxis.scale(halfDepth * 0.82f)),
                innerLip.subtract(depthAxis.scale(halfDepth * 0.55f)),
                innerBelly.subtract(depthAxis.scale(halfDepth * 0.62f)),
                innerShoulder.subtract(depthAxis.scale(halfDepth * 0.78f)),
                innerTop.subtract(depthAxis.scale(halfDepth * 0.90f))
        };

        short[] facesA = new short[]{
                // front plate
                0, 1, 7, 1, 6, 7,
                1, 2, 6, 2, 5, 6,
                2, 3, 5, 3, 4, 5,
                // back plate
                8, 15, 9, 9, 15, 14,
                9, 14, 10, 10, 14, 13,
                10, 13, 11, 11, 13, 12
        };

        short[] facesB = new short[]{
                0, 8, 9, 9, 1, 0,
                1, 9, 10, 10, 2, 1,
                2, 10, 11, 11, 3, 2,
                3, 11, 12, 12, 4, 3,
                4, 12, 13, 13, 5, 4,
                5, 13, 14, 14, 6, 5,
                6, 14, 15, 15, 7, 6,
                7, 15, 8, 8, 0, 7
        };

        short[] edges = new short[]{
                0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 0,
                8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 8,
                0, 8, 1, 9, 2, 10, 3, 11, 4, 12, 5, 13, 6, 14, 7, 15
        };

        // Colore unico sulle due valve: niente faccia arancione/grigia separata.
        drawSolid(gl, pts, bucketColor, bucketColor, facesA, facesB, edges);

        /*
         * Bordatura 3D reale della clamshell.
         * Serve per leggere meglio la forma da lontano.
         */
        float[] borderColor = GL_Methods.darkenColor(bucketColor.clone(), 0.38f, bucketColor[3]);
        float borderRadius = Math.max(0.026f * rs(), 0.012f);

        drawClamshellBorder(gl, pts, borderRadius, borderColor);
    }

    private static Point3DF[] makeBox(Point3DF center, Point3DF forward, Point3DF side, Point3DF up,
                                      float length, float width, float height) {
        Point3DF lf = forward.normalize().scale(length * 0.5f);
        Point3DF sd = side.normalize().scale(width * 0.5f);
        Point3DF vz = up.normalize().scale(height * 0.5f);

        Point3DF topFrontLeft = center.add(lf).subtract(sd).add(vz);
        Point3DF topFrontRight = center.add(lf).add(sd).add(vz);
        Point3DF topRearRight = center.subtract(lf).add(sd).add(vz);
        Point3DF topRearLeft = center.subtract(lf).subtract(sd).add(vz);

        Point3DF bottomFrontLeft = center.add(lf).subtract(sd).subtract(vz);
        Point3DF bottomFrontRight = center.add(lf).add(sd).subtract(vz);
        Point3DF bottomRearRight = center.subtract(lf).add(sd).subtract(vz);
        Point3DF bottomRearLeft = center.subtract(lf).subtract(sd).subtract(vz);

        return new Point3DF[]{
                topFrontLeft, topFrontRight, topRearRight, topRearLeft,
                bottomFrontLeft, bottomFrontRight, bottomRearRight, bottomRearLeft
        };
    }

    private static short[] boxLight() {
        return new short[]{
                0, 1, 2, 2, 3, 0,
                0, 4, 5, 5, 1, 0,
                1, 5, 6, 6, 2, 1
        };
    }

    private static short[] boxDark() {
        return new short[]{
                3, 2, 6, 6, 7, 3,
                0, 3, 7, 7, 4, 0,
                4, 7, 6, 6, 5, 4
        };
    }

    private static short[] boxEdges() {
        return new short[]{
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };
    }

    private static void drawSolid(GL11 gl, Point3DF[] points, float[] lightColor, float[] darkColor,
                                  short[] light, short[] dark, short[] edges) {
        if (points == null || points.length == 0) return;
        BoomsDrawer solid = new BoomsDrawer(points, light, lightColor, dark, darkColor, edges);
        solid.draw(gl);
    }

    private static Section sectionAt(Point3DF root, Point3DF tip,
                                     Point3DF sideAxis, Point3DF depthAxis,
                                     float width0, float depth0,
                                     float width1, float depth1,
                                     float t) {
        Point3DF center = root.add(tip.subtract(root).scale(t));
        float width = width0 + (width1 - width0) * t;
        float depth = depth0 + (depth1 - depth0) * t;
        return section(center, sideAxis, depthAxis, width, depth);
    }

    private static Section section(Point3DF center, Point3DF sideAxis, Point3DF depthAxis,
                                   float width, float depth) {
        float hw = width * 0.5f;
        float hd = depth * 0.5f;
        return new Section(
                center.subtract(sideAxis.scale(hw)).add(depthAxis.scale(hd)),
                center.add(sideAxis.scale(hw)).add(depthAxis.scale(hd)),
                center.subtract(sideAxis.scale(hw)).subtract(depthAxis.scale(hd)),
                center.add(sideAxis.scale(hw)).subtract(depthAxis.scale(hd))
        );
    }

    private static void drawTube(GL11 gl, Point3DF a, Point3DF b, float radius, float[] color, int sides) {
        if (!valid(a) || !valid(b)) return;
        if (a.subtract(b).length() < 0.0005f) return;

        Cylinder cylinder = new Cylinder(
                p3tof(a),
                p3tof(b),
                radius,
                radius,
                color,
                sides,
                true
        );
        cylinder.draw(gl);
    }

    private static Point3DF worldToGL(double[] p) {
        if (p == null || p.length < 3 || DataSaved.glL_AnchorView == null || DataSaved.glL_AnchorView.length < 3) {
            return null;
        }
        return pTransform(p, DataSaved.glL_AnchorView, rs());
    }

    private static Point3DF average4(Point3DF a, Point3DF b, Point3DF c, Point3DF d) {
        return new Point3DF(
                (a.getX() + b.getX() + c.getX() + d.getX()) * 0.25f,
                (a.getY() + b.getY() + c.getY() + d.getY()) * 0.25f,
                (a.getZ() + b.getZ() + c.getZ() + d.getZ()) * 0.25f
        );
    }

    private static Point3DF midpoint(Point3DF a, Point3DF b) {
        return new Point3DF((a.getX() + b.getX()) * 0.5f,
                (a.getY() + b.getY()) * 0.5f,
                (a.getZ() + b.getZ()) * 0.5f);
    }

    private static Point3DF flatten(Point3DF p) {
        return new Point3DF(p.getX(), p.getY(), 0f);
    }

    private static boolean valid(Point3DF p) {
        return p != null
                && !Float.isNaN(p.getX()) && !Float.isInfinite(p.getX())
                && !Float.isNaN(p.getY()) && !Float.isInfinite(p.getY())
                && !Float.isNaN(p.getZ()) && !Float.isInfinite(p.getZ());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{point3DF.getX(), point3DF.getY(), point3DF.getZ()};
    }

    private static class Section {
        final Point3DF topLeft;
        final Point3DF topRight;
        final Point3DF bottomLeft;
        final Point3DF bottomRight;

        Section(Point3DF topLeft, Point3DF topRight, Point3DF bottomLeft, Point3DF bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }
    }

    private static class Axes {
        final Point3DF forward;
        final Point3DF side;
        final Point3DF up;

        Axes(Point3DF forward, Point3DF side, Point3DF up) {
            this.forward = forward;
            this.side = side;
            this.up = up;
        }
    }
    private static void drawToolAlignCross(GL11 gl,
                                           Point3DF bottom,
                                           Point3DF vertical,
                                           Point3DF forward,
                                           Point3DF lateral,
                                           float toolHeight,
                                           float scale) {
        if (!valid(bottom)) return;

        Point3DF down = vertical.normalize();
        if (down.length() < 0.001f) down = new Point3DF(0f, 0f, -1f);

        Point3DF fw = forward.normalize();
        if (fw.length() < 0.001f) fw = new Point3DF(0f, 1f, 0f);

        Point3DF lat = lateral.normalize();
        if (lat.length() < 0.001f) lat = new Point3DF(1f, 0f, 0f);

        /*
         * Punto operativo leggermente sotto al grab,
         * così non viene coperto dal tool.
         */
        Point3DF start = bottom.add(down.scale(Math.max(0.08f * scale, 0.025f)));

        /*
         * Misure reali:
         * avanti 50 m
         * dietro 15 m
         * destra/sinistra 15 m
         */
        float fwLen = 50.0f * scale;
        float bwLen = 15.0f * scale;
        float sideLen = 15.0f * scale;

        /*
         * Spessore visibile
         */
        float lineRadius = Math.max(0.15f * scale, 0.050f);
        float[] alignColor = new float[]{0f, 1f, 0f, 1f};

        Point3DF fwP = start.add(fw.scale(fwLen));
        Point3DF bwP = start.subtract(fw.scale(bwLen));
        Point3DF ltP = start.subtract(lat.scale(sideLen));
        Point3DF rtP = start.add(lat.scale(sideLen));

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        try {
            drawTube(gl, start, fwP, lineRadius, alignColor, 8);
            drawTube(gl, start, bwP, lineRadius, alignColor, 8);
            drawTube(gl, start, ltP, lineRadius, alignColor, 8);
            drawTube(gl, start, rtP, lineRadius, alignColor, 8);
        } finally {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }


    }
    private static void drawClamshellBorder(GL11 gl,
                                            Point3DF[] pts,
                                            float radius,
                                            float[] color) {
        if (pts == null || pts.length < 16) return;

        /*
         * Contorno faccia frontale.
         */
        drawTube(gl, pts[0], pts[1], radius, color, 6);
        drawTube(gl, pts[1], pts[2], radius, color, 6);
        drawTube(gl, pts[2], pts[3], radius, color, 6);
        drawTube(gl, pts[3], pts[4], radius, color, 6);
        drawTube(gl, pts[4], pts[5], radius, color, 6);
        drawTube(gl, pts[5], pts[6], radius, color, 6);
        drawTube(gl, pts[6], pts[7], radius, color, 6);
        drawTube(gl, pts[7], pts[0], radius, color, 6);

        /*
         * Contorno faccia posteriore.
         */
        drawTube(gl, pts[8], pts[9], radius, color, 6);
        drawTube(gl, pts[9], pts[10], radius, color, 6);
        drawTube(gl, pts[10], pts[11], radius, color, 6);
        drawTube(gl, pts[11], pts[12], radius, color, 6);
        drawTube(gl, pts[12], pts[13], radius, color, 6);
        drawTube(gl, pts[13], pts[14], radius, color, 6);
        drawTube(gl, pts[14], pts[15], radius, color, 6);
        drawTube(gl, pts[15], pts[8], radius, color, 6);

        /*
         * Collegamenti di profondità: danno volume e leggibilità.
         */
        drawTube(gl, pts[0], pts[8], radius, color, 6);
        drawTube(gl, pts[1], pts[9], radius, color, 6);
        drawTube(gl, pts[2], pts[10], radius, color, 6);
        drawTube(gl, pts[3], pts[11], radius, color, 6);
        drawTube(gl, pts[4], pts[12], radius, color, 6);
        drawTube(gl, pts[5], pts[13], radius, color, 6);
        drawTube(gl, pts[6], pts[14], radius, color, 6);
        drawTube(gl, pts[7], pts[15], radius, color, 6);
    }
}
