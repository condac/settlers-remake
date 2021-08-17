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
package jsettlers.main.datatypes;

import jsettlers.common.menu.IMultiplayerPlayer;
import jsettlers.network.common.packets.PlayerInfoPacket;
import jsettlers.common.player.ECivilisation;
/**
 * 
 * @author Andreas Eberle
 * 
 */
public class MultiplayerPlayer implements IMultiplayerPlayer {

	private final String id;
	private final String name;
	private final boolean ready;
	private final byte teamId;
	private final ECivilisation civ;

	public MultiplayerPlayer(PlayerInfoPacket playerInfoPacket) {
		this.id = playerInfoPacket.getId();
		this.name = playerInfoPacket.getName();
		this.ready = playerInfoPacket.isReady();
		this.teamId = playerInfoPacket.getTeamId();
		
		switch (playerInfoPacket.getCivilisation()) {
			case 1:
				this.civ = ECivilisation.ROMAN;
				break;
			case 2:
				this.civ = ECivilisation.EGYPTIAN;
				break;
			case 3:
				this.civ = ECivilisation.ASIAN;
				break;
			case 4:
				this.civ = ECivilisation.AMAZON;
				break;
			default:
				this.civ = ECivilisation.ROMAN;
				break;
		}
	
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	public byte getTeamId() {
		return teamId;
	}
	
	@Override
	public ECivilisation getCivilisation() {
		return civ;
	}

	@Override
	public String toString() {
		return "MultiplayerPlayer [id=" + id + ", name=" + name + ", ready=" + ready + "]";
	}
}
