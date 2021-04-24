package edu.cwru.sepia.agent.planner;

public class Peasant {
		private int id;
		private Position position;
		private int numGold = 0;
		private int numWood = 0;
		
		public Peasant(int id, Position position) {
			this.id = id;
			this.position = position;
		}
		
		public Peasant(Peasant value) {
			this.id = value.id;
			this.position = new Position(value.position);
			this.numGold = value.numGold;
			this.numWood = value.numWood;
		}
		
		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}
		
		public Position getPosition() {
			return position;
		}
		public void setPosition(Position position) {
			this.position = position;
		}
		
		public int getNumGold() {
			return numGold;
		}
		
		public void setNumGold(int numGold) {
			this.numGold = numGold;
		}
		
		public int getNumWood() {
			return numWood;
		}
		
		public void setNumWood(int numWood) {
			this.numWood = numWood;
		}
		
		public boolean hasGold() {
			return numGold > 0;
		}
		
		public boolean hasWood() {
			return numWood > 0;
		}
		
		public boolean hasResource() {
			return hasGold() || hasWood();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + numGold;
			result = prime * result + numWood;
			result = prime * result + ((position == null) ? 0 : position.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Peasant other = (Peasant) obj;
			if (id != other.id)
				return false;
			if (numGold != other.numGold)
				return false;
			if (numWood != other.numWood)
				return false;
			if (position == null) {
				if (other.position != null)
					return false;
			} else if (!position.equals(other.position))
				return false;
			return true;
		}		
		
	}