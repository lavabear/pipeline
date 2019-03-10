import React, { Component } from 'react';
import './App.css';
import { Redirect } from 'react-router'
import { Route, Link } from 'react-router-dom'
import { Home } from './Home';
import { Commands } from './Command';
import { Editor } from './Editor';
import { ToastContainer } from 'react-toastify';
// import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import axios from "axios";

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {commands: [], pipelineRequest: [], selected: undefined, previewCommand: undefined}
        this.changeSelected = this.changeSelected.bind(this);
        this.showCommand = this.showCommand.bind(this);
    }

    componentDidMount() {
        axios.get('https://pipeline-ws.herokuapp.com/pipeline/api/commands')
            .then(response => {
                this.setState({commands: response.data})
            }).catch(console.error)
    }

    changeSelected(selected) {
        this.setState({selected: this.state.commands[selected.currentTarget.value]})
    }

    showCommand(raw) {
        return (event) => this.setState({previewCommand: raw})
    }

    render() {
        return (
        <div className="App">
          <ul>
              <li><Link to="/home" title="Home">Home</Link></li>
              <li><Link to="/commands" title="Commands">Commands</Link></li>
              <li><Link to="/editor" title="Editor">Editor</Link></li>
          </ul>
          <ToastContainer />
          <Route exact path="/" component={() => <Redirect to="/home" />}/>
          <Route exact path="/home" component={() =>
              Home(this.state.commands, this.state.pipelineRequest, this.state.selected, this.changeSelected)}/>
          <Route exact path="/commands" component={() => Commands(this.state.commands, this.state.previewCommand, this.showCommand)}/>
          <Route exact path="/editor" component={() => Editor(this.state.commands, this.state.previewCommand, this.showCommand)}/>
        </div>
        );
    }
}

export default App;
