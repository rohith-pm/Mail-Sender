import React, { useRef } from 'react';
import { Card } from 'react-bootstrap';
import { AiOutlineCloseCircle } from "react-icons/ai";

const Attachment = props => {
    const att = useRef(null);
    const applyBg = () => {
        att.current.style.backgroundColor = "#ffc107";
    }
    const removeBg = () => {
        att.current.style.backgroundColor = "white";
    }
    return(<Card ref={att} onMouseEnter={applyBg} onMouseLeave={removeBg} style={{
        margin: 8,
    }}>
        <Card.Body>
                {props.filename+" "}    
            <AiOutlineCloseCircle size="" onClick={props.removeAttachment}/>
        </Card.Body>
    </Card>);
}

export default Attachment;