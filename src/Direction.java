/**
 * Enumeration representing directions for movement in a 2D space.
 */
public enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

        private final int xOffset;
        private final int yOffset;

        /**
         * Constructor for Direction enum.
         *
         * @param xOffset Horizontal offset value
         * @param yOffset Vertical offset value
         */
        Direction(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        /**
         * Returns the horizontal offset value for this direction.
         *
         * @return Horizontal offset
         */
        public int getXOffset() {
            return xOffset;
        }

        /**
         * Returns the vertical offset value for this direction.
         *
         * @return Vertical offset
         */
        public int getYOffset() {
            return yOffset;
        }
    }