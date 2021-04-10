package edu.cwru.sepia.agent.planner.resources;

import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Resource {
   protected int id;
   protected int amount;
   protected Position position;
   private ResourceNode.Type type;

   public Resource(int id, int amount, Position position, ResourceNode.Type type) {
      this.position = position;
      this.amount = amount;
      this.type = type;
      this.id = id;
   }

   public Position getPosition() {
      return position;
   }
}
