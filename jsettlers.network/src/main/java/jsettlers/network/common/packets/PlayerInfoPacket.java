/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.network.common.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import jsettlers.network.infrastructure.channel.packet.Packet;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public class PlayerInfoPacket extends Packet {
	private String id;
	private String name;
	private boolean ready;
	private boolean startFinished;
	private byte teamId;
	private byte civ;

	public PlayerInfoPacket() {
	}

	public PlayerInfoPacket(String id, String name, boolean ready, byte teamId, byte civIn) {
		this.id = id;
		this.name = name;
		this.ready = ready;
		this.teamId = teamId;
		this.civ = civIn;
		
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeUTF(id);
		dos.writeUTF(name);
		dos.writeBoolean(ready);
		dos.writeBoolean(startFinished);
		dos.writeByte(teamId);
		dos.writeByte(civ);
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		id = dis.readUTF();
		name = dis.readUTF();
		ready = dis.readBoolean();
		startFinished = dis.readBoolean();
		teamId = dis.readByte();
		civ = dis.readByte();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public byte getTeamId() {
		return teamId;
	}
	
	public byte getCivilisation() {
		return civ;
		
	}
	
	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public void setTeamId(byte teamId) {
		this.teamId = teamId;
	}

	public void setCivilisation(byte civIn) {
		this.civ = civIn;
		
	}

	public boolean isReady() {
		return ready;
	}

	public boolean isStartFinished() {
		return startFinished;
	}

	public void setStartFinished(boolean startFinished) {
		this.startFinished = startFinished;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (ready ? 1231 : 1237);
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
		PlayerInfoPacket other = (PlayerInfoPacket) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return ready == other.ready;
	}

}
