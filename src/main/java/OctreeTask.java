/**
 * @author Yex
 */
public class OctreeTask {

    public class OctTree {
        //coordinates
        int x;
        int y;
        int z;

        int width;
        int height;
        int depth;

        public OctTree(int width, int height, int depth) {
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        public OctTree(int width, int height, int depth, int x, int y, int z) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Splits octree into 8 subtrees.
         * @return
         */
        public OctTree[] split() {
            OctTree[] res = new OctTree[8];
            int w2 = width / 2;
            int h2 = height / 2;
            int d2 = depth / 2;
            res[0] = new OctTree(w2, h2, d2, x, y, z);
            res[1] = new OctTree(w2, h2, d2, x + w2, y, z);
            res[2] = new OctTree(w2, h2, d2, x, y + h2, z);
            res[3] = new OctTree(w2, h2, d2, x, y, z + d2);
            res[4] = new OctTree(w2, h2, d2, x + w2, y + h2, z);
            res[5] = new OctTree(w2, h2, d2, x, y + h2, z + d2);
            res[6] = new OctTree(w2, h2, d2, x + w2, y, z + d2);
            res[7] = new OctTree(w2, h2, d2, x + w2, y + h2, z + d2);
            return res;
        }
    }

}
